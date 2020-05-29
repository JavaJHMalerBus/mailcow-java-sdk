package de.fheuschen.mailcow.sdk.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import de.fheuschen.mailcow.sdk.Mailcow;
import de.fheuschen.mailcow.sdk.builder.DomainBuilder;
import de.fheuschen.mailcow.sdk.exception.MailcowException;
import de.fheuschen.mailcow.sdk.model.Domain;
import de.fheuschen.mailcow.sdk.model.MailcowModel;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * Client
 *
 * @author Julian B. Heuschen <julian@fheuschen.de>
 */
public abstract class BaseClient {

    protected Client client = ClientBuilder.newClient();
    protected String apiKey;
    protected WebTarget server;
    protected Mailcow m;
    protected static Gson g = new Gson();

    public void initialize(Mailcow m) {
        this.m = m;
    }

    /**
     * This method performs the neccessary authentication on the WebTarget object (i.e., adding api keys or similar)
     * @param t the target
     * @return the authenticated target
     */
    protected abstract Invocation.Builder doAuthentication(Invocation.Builder t);

    /**
     * Performs a request and returns the raw response
     * @param endpoint
     * @param params
     * @return
     */
    public Response performGetRequest(Endpoint<?> endpoint, Map<String, String> params) {
        WebTarget t = server.path(endpoint.getEndpointUrl());
        for(String key : params.keySet())
            t.queryParam(key, params.getOrDefault(key, ""));
        return this.doAuthentication(t.request(MediaType.APPLICATION_JSON)).get();
    }

    /**
     * Performs a request
     * @param endpoint
     * @param params
     * @param clazz
     * @param <T>
     * @return
     */
    public <T extends MailcowModel> T performGetRequest(Endpoint<T> endpoint, Map<String, String> params, Class<T> clazz) {
        WebTarget t = server.path(endpoint.getEndpointUrl());
        if(params != null)
            for(String key : params.keySet())
                t.queryParam(key, params.getOrDefault(key, ""));
        return this.doAuthentication(t.request(MediaType.APPLICATION_JSON)).get(clazz);
    }

    public <T extends MailcowModel> Collection<T> performMultiGetRequest(Endpoint<T> endpoint, Map<String, String> params, Class<T> clazz, String id) {
        WebTarget t = server.path(endpoint.getEndpointUrl() + ((id == null) ? "" : id));
        //WebTarget t = server;
        if(params != null)
            for(String key : params.keySet())
                t.queryParam(key, params.getOrDefault(key, ""));
        Response r = this.doAuthentication(t.request(MediaType.APPLICATION_JSON)).get();
        return g.fromJson(r.readEntity(String.class), new TypeToken<ArrayList<T>>() {}.getType());
    }

    public Response performDelete(MailcowModel m, Map<String, Object> params) {
        WebTarget t = server.path(m.getEndpoint().getDeleteEndpointUrl());
        return this.doAuthentication(t.request(MediaType.APPLICATION_JSON)).post(Entity.entity(params, MediaType.APPLICATION_JSON));
    }

    public boolean connectionSuccessful() {
        try {
            Collection<Domain> d = new DomainBuilder()
                    .fetchAll(m);
            return d != null;
        } catch (MailcowException e) {
            e.printStackTrace();
            return false;
        }
    }

    public interface Endpoint<T extends MailcowModel > {
        String getEndpointUrl();
        String getEditEndpointUrl();
        String getDeleteEndpointUrl();
        String getAddEndpointUrl();
    }



}