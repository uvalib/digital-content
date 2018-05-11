package edu.virginia.lib.content;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Properties;

@Path("/")
public class ContentMediator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContentMediator.class);

    private static File getContentPath() {
        if (System.getenv("CONTENT_PATH") == null) {
            throw new RuntimeException("The environment variable, 'CONTENT_PATH', must be set to the root path of the content to be exposed.");
        }
        return new File(System.getenv("CONTENT_PATH"));
    }

    private static File getPublicContentPath(final String id) {
        return new File(getContentPath(), "public/" + id);
    }

    private static File getUVAContentPath(final String id) {
        return new File(getContentPath(), "uva/" + id);
    }

    @Path("version")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response version() {
        try {
            Properties p = new Properties();
            p.load(this.getClass().getClassLoader().getResourceAsStream("version.properties"));
            return Response.status(200).entity(Json.createObjectBuilder().add("version", p.getProperty("version")).build()).build();
        } catch (IOException e) {
            LOGGER.error("Error serving /status.", e);
            return Response.status(500).build();
        }
    }

    @Path("healthcheck")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response healthcheck() {
        return Response.status(200).entity(Json.createObjectBuilder().add("content_fs",
                Json.createObjectBuilder().add("healthy", getContentPath().exists()
                        && getContentPath().canRead())).build()).build();
    }

    /**
     * The persitent URL endpoint.  Requests against this URL pattern will redirect to the appropriate file for the
     * resource identified in the path.
     */
    @Path("{id}")
    @GET
    public Response getResource(@PathParam("id") final String id) {
        try {
            final File publicFile = getPublicContentPath(id);
            final File uvaFile = getUVAContentPath(id);
            if (publicFile.exists()) {
                return redirectToFile(id, publicFile, null);
            } else if (uvaFile.exists()) {
                return redirectToFile(id, uvaFile, "uva");
            } else {
                return Response.status(404).build();
            }
        } catch (URISyntaxException e) {
            LOGGER.error("Error serving item.", e);
            return Response.status(500).build();
        }
    }

    private Response redirectToFile(final String id, final File file, final String authpath) throws URISyntaxException {
        File[] files = file.listFiles((File dir, String name) -> {
            return !name.startsWith(".");
        });
        if (files.length != 1) {
            LOGGER.error("There must be exactly one non-hidden file for a given identifier. ("
                    + file.getAbsolutePath() + " has " + files.length + ")");
            return Response.status(404).build();
        } else {
            return Response.temporaryRedirect(new URI((authpath != null && !authpath.equals("") ? "/" + authpath : "")
                            + "/" + id + "/" + files[0].getName())).build();
        }
    }

    @Path("{id}/{filename}")
    @GET
    public Response getFile(@PathParam("id") final String id, @PathParam("filename") final String filename) {
        final File publicFile = new File(getPublicContentPath(id), filename);
        if (publicFile.exists()) {
            try {
                return Response.status(200).entity(publicFile).type(Files.probeContentType(publicFile.toPath())).build();
            } catch (IOException e) {
                LOGGER.error("Error probing mime type for " + publicFile + ".", e);
                return Response.status(500).build();
            }
        } else {
            return Response.status(404).build();
        }
    }

    /**
     * It is assumed that access to this path will be limited to authentic UVA users by an external mechanism.
     */
    @Path("uva/{id}/{filename}")
    @GET
    public Response getUvaFile(@PathParam("id") final String id, @PathParam("filename") final String filename) {
        final File uvaFile = new File(getUVAContentPath(id), filename);
        if (uvaFile.exists()) {
            try {
                return Response.status(200).entity(uvaFile).type(Files.probeContentType(uvaFile.toPath())).build();
            } catch (IOException e) {
                LOGGER.error("Error probing mime type for " + uvaFile + ".", e);
                return Response.status(500).build();
            }
        } else {
            return Response.status(404).build();
        }
    }

}
