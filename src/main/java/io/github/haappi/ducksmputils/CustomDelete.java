package io.github.haappi.ducksmputils;


import org.apache.http.client.methods.HttpPost;

class CustomDelete extends HttpPost {
    public CustomDelete(String url) {
        super(url);
    }

    @Override
    public String getMethod() {
        return "DELETE";
    }
}