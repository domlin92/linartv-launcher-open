package com.linart.tvpreview;

import android.graphics.BitmapFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

/** Short-lived, PIN-protected photo receiver. It runs only while its TV dialog is open. */
final class PhotoUploadServer implements AutoCloseable {
    static final int PORT = 8765;
    private static final long MAX_BYTES = 25_000_000;
    /** After this many wrong codes the receiver refuses uploads until the TV dialog is reopened. */
    private static final int MAX_FAILED_PINS = 5;
    private int failedPins;
    private final File folder;
    private final Translations translations;
    private final Consumer<String> onUploaded;
    private final String pin = String.format(Locale.ROOT, "%06d", new SecureRandom().nextInt(1_000_000));
    private final ServerSocket server;
    private final Thread worker;
    private volatile boolean running = true;

    PhotoUploadServer(File folder, Translations translations, Consumer<String> onUploaded) throws IOException {
        this.folder = folder;
        this.translations = translations;
        this.onUploaded = onUploaded;
        server = new ServerSocket(PORT);
        worker = new Thread(this::serve, "wallpaper-upload");
        worker.setDaemon(true);
        worker.start();
    }

    String pin() { return pin; }
    private String tr(String polish, String englishText) { return translations.tr(polish, englishText); }

    static String localIpv4() {
        String fallback = null;
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface network = interfaces.nextElement();
                if (!network.isUp() || network.isLoopback()) continue;
                Enumeration<InetAddress> addresses = network.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (!(address instanceof Inet4Address) || !address.isSiteLocalAddress()) continue;
                    if (network.getName().startsWith("wlan")) return address.getHostAddress();
                    if (fallback == null) fallback = address.getHostAddress();
                }
            }
        } catch (Exception ignored) { }
        return fallback;
    }

    private void serve() {
        while (running) {
            try (Socket socket = server.accept()) {
                socket.setSoTimeout(15_000);
                handle(socket);
            } catch (IOException ignored) {
                if (!running) return;
            }
        }
    }

    private void handle(Socket socket) throws IOException {
        BufferedInputStream input = new BufferedInputStream(socket.getInputStream());
        OutputStream output = socket.getOutputStream();
        String request = readLine(input);
        if (request == null) return;
        Map<String, String> headers = new HashMap<>();
        String line;
        while ((line = readLine(input)) != null && !line.isEmpty()) {
            int colon = line.indexOf(':');
            if (colon > 0) headers.put(line.substring(0, colon).trim().toLowerCase(Locale.ROOT),
                    line.substring(colon + 1).trim());
        }
        if (request.startsWith("GET / ")) {
            respond(output, 200, "text/html; charset=utf-8", page());
        } else if (request.startsWith("POST /upload ")) {
            receive(input, output, headers);
        } else {
            respond(output, 404, "text/plain; charset=utf-8", tr("Nie znaleziono strony", "Page not found"));
        }
    }

    private void receive(BufferedInputStream input, OutputStream output, Map<String, String> headers)
            throws IOException {
        if (failedPins >= MAX_FAILED_PINS) {
            respond(output, 429, "text/plain; charset=utf-8",
                    tr("Zbyt wiele błędnych kodów. Zamknij okno na TV i otwórz je ponownie, aby dostać nowy kod.",
                            "Too many incorrect codes. Close and reopen the TV window for a new code."));
            return;
        }
        if (!pin.equals(headers.get("x-pin"))) {
            failedPins++;
            respond(output, 403, "text/plain; charset=utf-8", tr("Nieprawidłowy kod z telewizora", "Incorrect TV code"));
            return;
        }
        long length;
        try { length = Long.parseLong(headers.getOrDefault("content-length", "-1")); }
        catch (NumberFormatException e) { length = -1; }
        boolean chunked = "chunked".equalsIgnoreCase(headers.getOrDefault("transfer-encoding", ""));
        if ((!chunked && length <= 0) || length > MAX_BYTES) {
            respond(output, 413, "text/plain; charset=utf-8", tr("Plik musi mieć mniej niż 25 MB", "File must be under 25 MB"));
            return;
        }
        String original;
        try { original = URLDecoder.decode(headers.getOrDefault("x-filename", "zdjecie"), "UTF-8"); }
        catch (Exception e) { original = "zdjecie"; }
        File temp = File.createTempFile("upload-", ".part", folder);
        try {
            try (FileOutputStream file = new FileOutputStream(temp)) {
                byte[] buffer = new byte[8192];
                if (chunked) {
                    long total = 0;
                    while (true) {
                        String header = readLine(input);
                        if (header == null) throw new IOException("Niepełny plik");
                        int semicolon = header.indexOf(';');
                        String size = (semicolon < 0 ? header : header.substring(0, semicolon)).trim();
                        long chunk;
                        try { chunk = Long.parseLong(size, 16); }
                        catch (NumberFormatException e) { throw new IOException("Błędny rozmiar fragmentu", e); }
                        if (chunk == 0) {
                            while ((header = readLine(input)) != null && !header.isEmpty()) { }
                            break;
                        }
                        if (chunk < 0 || chunk > MAX_BYTES - total) {
                            respond(output, 413, "text/plain; charset=utf-8", tr("Plik musi mieć mniej niż 25 MB", "File must be under 25 MB"));
                            return;
                        }
                        copyBytes(input, file, buffer, chunk);
                        total += chunk;
                        if (input.read() != '\r' || input.read() != '\n')
                            throw new IOException("Błędny format fragmentu");
                    }
                } else {
                    copyBytes(input, file, buffer, length);
                }
            }
            String extension = imageExtension(temp);
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(temp.getAbsolutePath(), bounds);
            if (extension == null || bounds.outWidth < 1 || bounds.outHeight < 1
                    || bounds.outWidth > 20_000 || bounds.outHeight > 20_000) {
                respond(output, 415, "text/plain; charset=utf-8", tr("Wybierz zdjęcie JPG, PNG lub WebP",
                        "Choose a JPG, PNG or WebP photo"));
                return;
            }
            String stem = original.replaceAll("(?i)\\.(jpg|jpeg|png|webp)$", "")
                    .replaceAll("[^\\p{L}\\p{N}._ -]", "_").trim();
            if (stem.isEmpty()) stem = "zdjecie";
            if (stem.length() > 45) stem = stem.substring(0, 45);
            File target = new File(folder, System.currentTimeMillis() + "-" + stem + "." + extension);
            if (!temp.renameTo(target)) throw new IOException("Nie udało się zapisać zdjęcia");
            onUploaded.accept(target.getName());
            respond(output, 200, "text/plain; charset=utf-8", tr("Dodano: ", "Added: ") + target.getName());
        } finally {
            temp.delete();
        }
    }

    private static void copyBytes(BufferedInputStream input, FileOutputStream file,
            byte[] buffer, long length) throws IOException {
        long remaining = length;
        while (remaining > 0) {
            int read = input.read(buffer, 0, (int) Math.min(buffer.length, remaining));
            if (read < 0) throw new IOException("Niepełny plik");
            file.write(buffer, 0, read);
            remaining -= read;
        }
    }

    private static String imageExtension(File file) throws IOException {
        byte[] bytes = new byte[12];
        try (FileInputStream input = new FileInputStream(file)) {
            if (input.read(bytes) < bytes.length) return null;
        }
        if ((bytes[0] & 255) == 0xFF && (bytes[1] & 255) == 0xD8 && (bytes[2] & 255) == 0xFF)
            return "jpg";
        if ((bytes[0] & 255) == 0x89 && bytes[1] == 'P' && bytes[2] == 'N' && bytes[3] == 'G')
            return "png";
        if (bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P')
            return "webp";
        return null;
    }

    private static String readLine(BufferedInputStream input) throws IOException {
        StringBuilder line = new StringBuilder();
        int value;
        while ((value = input.read()) != -1) {
            if (value == '\n') return line.toString();
            if (value != '\r') line.append((char) value);
            if (line.length() > 8192) throw new IOException("Za długi nagłówek");
        }
        return line.length() == 0 ? null : line.toString();
    }

    private static void respond(OutputStream output, int code, String type, String body) throws IOException {
        byte[] payload = body.getBytes(StandardCharsets.UTF_8);
        String status = code == 200 ? "OK" : code == 403 ? "Forbidden"
                : code == 413 ? "Payload Too Large" : code == 429 ? "Too Many Requests" : code == 415 ? "Unsupported Media Type" : "Not Found";
        String headers = "HTTP/1.1 " + code + " " + status + "\r\nContent-Type: " + type
                + "\r\nContent-Length: " + payload.length
                + "\r\nCache-Control: no-store\r\nConnection: close\r\n\r\n";
        output.write(headers.getBytes(StandardCharsets.US_ASCII));
        output.write(payload);
        output.flush();
    }

    private String page() {
        String html = "<!doctype html><html lang=pl><meta charset=utf-8><meta name=viewport content='width=device-width,initial-scale=1'>"
                + "<title>LinarTV · zdjęcia</title><style>body{margin:0;min-height:100vh;background:#111d38;"
                + "color:#f6f8ff;font:18px system-ui;display:grid;place-items:center}main{width:min(90vw,520px);"
                + "background:#213454;border:1px solid #6d87ac;border-radius:24px;padding:28px;box-sizing:border-box}"
                + "h1{font-size:30px;margin:0 0 12px}p{color:#c8d4e8;line-height:1.45}label{display:block;margin:18px 0 8px}"
                + "input{box-sizing:border-box;width:100%;font:inherit;padding:13px;border-radius:12px;border:1px solid #9cb2d0}"
                + "input[type=file]{background:#f4f6fa;color:#17284b}button{width:100%;margin-top:22px;padding:15px;"
                + "border:0;border-radius:13px;background:#90cfff;color:#10213c;font:700 19px system-ui}"
                + "#status{min-height:2em;white-space:pre-line}</style><main><h1>Zdjęcia na TV</h1>"
                + "<p>Telefon i telewizor muszą być w tej samej sieci. Wybierz zdjęcia JPG, PNG lub WebP, do 25 MB każde.</p>"
                + "<label>Kod widoczny na TV</label><input id=pin inputmode=numeric pattern='[0-9]{6}' maxlength=6 placeholder='6 cyfr'>"
                + "<label>Zdjęcia</label><input id=photos type=file accept='image/jpeg,image/png,image/webp' multiple>"
                + "<button onclick='send()'>Dodaj zdjęcia</button><p id=status></p></main><script>"
                + "async function send(){let p=document.getElementById('pin').value,f=document.getElementById('photos').files,"
                + "s=document.getElementById('status');if(!/^[0-9]{6}$/.test(p)||!f.length){s.textContent='Wpisz kod i wybierz zdjęcia.';return}"
                + "s.textContent='Przesyłanie…';for(let i=0;i<f.length;i++){let x=f[i];if(x.size>25000000){s.textContent+='\\nZa duży plik: '+x.name;continue}"
                + "try{let r=await fetch('/upload',{method:'POST',headers:{'X-Pin':p,'X-Filename':encodeURIComponent(x.name),'Content-Type':x.type||'application/octet-stream'},body:x});"
                + "let t=await r.text();s.textContent+='\\n'+t}catch(e){s.textContent+='\\nNie udało się wysłać '+x.name+'. Sprawdź, czy okno z kodem jest nadal otwarte na TV, i spróbuj ponownie.'}}}</script></html>";
        if ("pl".equals(translations.code())) return html;
        return html.replace("lang=pl", "lang=" + translations.code())
                .replace("LinarTV · zdjęcia", htmlText(tr("LinarTV · zdjęcia", "LinarTV · photos")))
                .replace("Zdjęcia na TV", htmlText(tr("Zdjęcia na TV", "Photos on TV")))
                .replace("Telefon i telewizor muszą być w tej samej sieci. Wybierz zdjęcia JPG, PNG lub WebP, do 25 MB każde.", htmlText(tr("Telefon i telewizor muszą być w tej samej sieci. Wybierz zdjęcia JPG, PNG lub WebP, do 25 MB każde.", "Phone and TV must be on the same network. Choose JPG, PNG or WebP photos, up to 25 MB each.")))
                .replace("Kod widoczny na TV", htmlText(tr("Kod widoczny na TV", "Code shown on TV")))
                .replace("6 cyfr", htmlText(tr("6 cyfr", "6 digits")))
                .replace("Zdjęcia", htmlText(tr("Zdjęcia", "Photos")))
                .replace("Dodaj zdjęcia", htmlText(tr("Dodaj zdjęcia", "Add photos")))
                .replace("Wpisz kod i wybierz zdjęcia.", jsText(tr("Wpisz kod i wybierz zdjęcia.", "Enter the code and choose photos.")))
                .replace("Przesyłanie…", jsText(tr("Przesyłanie…", "Uploading…")))
                .replace("Za duży plik: ", jsText(tr("Za duży plik: ", "File too large: ")))
                .replace("Nie udało się wysłać ", jsText(tr("Nie udało się wysłać ", "Could not upload ")))
                .replace(". Sprawdź, czy okno z kodem jest nadal otwarte na TV, i spróbuj ponownie.", jsText(tr(". Sprawdź, czy okno z kodem jest nadal otwarte na TV, i spróbuj ponownie.", ". Check that the code window is still open on TV and try again.")));
    }


    private static String htmlText(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
    private static String jsText(String value) {
        return value.replace("\\", "\\\\").replace("'", "\\'")
                .replace("<", "\\u003c").replace("\n", "\\n").replace("\r", "\\r");
    }

    @Override public void close() {
        running = false;
        try { server.close(); } catch (IOException ignored) { }
    }
}
