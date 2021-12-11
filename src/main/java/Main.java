import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Main {
    public static void main(String[] args) {
       new Main().loadBingImages();
    }

    private void run() {
        final long start = System.nanoTime();
        System.out.printf("Started at: %d%n", start);

        OkHttpClient client = createOkHttpClient();

        Request request = new Request.Builder()
                .url("https://google.com")
                .build();

        try (Response response = client.newCall(request).execute()) {
            System.out.println(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }

        final long end = System.nanoTime();
        System.out.printf("Finished at %d. Duration: %.5fs %n", end, (end - start) / 1_000_000_000f);
    }

    private OkHttpClient createOkHttpClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(
                new HttpLoggingInterceptor.Logger() {
            public void log(String message) {
                System.out.println(String.format(
                        "[%d]: %s", Thread.currentThread().getId(), message));
            }
        });
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        return client;
    }

    private void loadBingImages() {
        OkHttpClient client = createOkHttpClient();

        Request request = new Request.Builder()
                .url("http://www.bing.com/HPImageArchive.aspx?format=js&idx=0&n=3&mkt=en-US")
                .build();

        try (Response response = client.newCall(request).execute()) {
            String json = response.body().string();

            Gson gson = new Gson();

            BingResponse bingResponse = gson.fromJson(json, BingResponse.class);

            for (BingImage image : bingResponse.images) {
                String fullUrl = "https://www.bing.com" + image.url;
                downloadImage(client, fullUrl);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void downloadImage(OkHttpClient client, String fullUrl) {
        Request request = new Request.Builder()
                .url(fullUrl)
                .build();

        try (Response response = client.newCall(request).execute()) {
            final InputStream inputStream = response.body().byteStream();

            OutputStream outputStream = new FileOutputStream("test.jpg");

            byte[] buffer = new byte[4096];

            while (true) {
                int readCount = inputStream.read();
                if (readCount < 0) {
                    break;
                }

                outputStream.write(buffer, 0, readCount);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
