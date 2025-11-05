package pack;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/Serv")
public class Serv extends HttpServlet {

    private final HttpClient client = HttpClient.newHttpClient();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String machine = request.getParameter("machine");
        String image = request.getParameter("image");

        if (machine == null) {
            InputStream file = request.getServletContext().getResourceAsStream("/" + image);
            byte[] bytes = file.readAllBytes();
            response.setContentType(getServletContext().getMimeType(image));
            response.setContentLength(bytes.length);
            response.getOutputStream().write(bytes);
            return;
        }

        String url = "http://"+machine+":8080/serv/Serv?image="+image;
        HttpRequest req = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .GET()
        .build();

        //System.out.println(url);
        try {
            HttpResponse<byte[]> resp;
            resp = client.send(req, HttpResponse.BodyHandlers.ofByteArray());

            resp.headers().firstValue("Content-Type")
                    .ifPresent(ct -> response.setContentType(ct));
            resp.headers().firstValue("Content-Length")
                    .ifPresent(cl -> response.setHeader("Content-Length", cl));
            resp.headers().allValues("Cache-Control")
                    .forEach(cc -> response.addHeader("Cache-Control", cc));
            response.getOutputStream().write(resp.body());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    }
}