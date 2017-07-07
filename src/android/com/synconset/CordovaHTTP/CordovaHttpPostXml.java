/**
 * A HTTP plugin for Cordova / Phonegap
 */
package com.synconset;

import java.net.UnknownHostException;
import java.util.Map;
import java.io.UnsupportedEncodingException;
import org.apache.cordova.CallbackContext;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import javax.net.ssl.SSLHandshakeException;

import android.util.Log;

import com.github.kevinsawicki.http.HttpRequest;
import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;

public class CordovaHttpPostXml extends CordovaHttp implements Runnable {
    public CordovaHttpPostXml(String urlString,String bodyXml, Map<String, String> headers, CallbackContext callbackContext) {
        super(urlString, bodyXml, headers, callbackContext);
    }
    @Override
    public void run() {
        try {
            HttpRequest request = HttpRequest.post(this.getUrlString());
            this.setupSecurity(request);
            request.acceptCharset(CHARSET);
            request.headers(this.getHeaders());
            request.accept("application/xml");
            request.contentType(HttpRequest.CONTENT_TYPE_XML);
            request.send(this.getBodyXml());
            //request.followRedirects(this.getIsFollowRedirects());
            int code = request.code();
            String body = request.body(CHARSET);
            JSONObject response = new JSONObject();
            this.addResponseHeaders(request, response);
            String bodyXMl = "";
            response.put("status", code);
            try {
              bodyXMl = new String(body, "UTF-8");
              body = new String(body.getBytes(), "UTF-8");
             } catch (UnsupportedEncodingException e) {

              }
              try {
                JSONObject xmlJSONObj = XML.toJSONObject(bodyXMl);
                body = xmlJSONObj.toString();
              } catch (JSONException e) {

              }
            if (code >= 200 && code < 300) {
                response.put("data", body);
                this.getCallbackContext().success(response);
            } else {
                response.put("error", body);
                this.getCallbackContext().error(response);
            }
        } catch (JSONException e) {
            this.respondWithError("There was an error generating the response");
        }  catch (HttpRequestException e) {
            if (e.getCause() instanceof UnknownHostException) {
                this.respondWithError(0, "The host could not be resolved");
            } else if (e.getCause() instanceof SSLHandshakeException) {
                this.respondWithError("SSL handshake failed");
            } else {
                this.respondWithError("There was an error with the request");
            }
        }
    }
}
