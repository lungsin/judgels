package judgels.jophiel.api.user;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.Map;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import judgels.service.api.actor.AuthHeader;

@Path("/api/v2/users")
public interface UserService {
    @GET
    @Path("/{userJid}")
    @Produces(APPLICATION_JSON)
    User getUser(@HeaderParam(AUTHORIZATION) AuthHeader authHeader, @PathParam("userJid") String userJid);

    @GET
    @Path("/username/{username}/exists")
    @Produces(APPLICATION_JSON)
    boolean usernameExists(@PathParam("username") String username);

    @GET
    @Path("/email/{email}/exists")
    @Produces(APPLICATION_JSON)
    boolean emailExists(@PathParam("email") String email);

    @GET
    @Path("/me")
    @Produces(APPLICATION_JSON)
    User getMyself(@HeaderParam(AUTHORIZATION) AuthHeader authHeader);

    @POST
    @Path("/me/password")
    @Consumes(APPLICATION_JSON)
    void updateMyPassword(@HeaderParam(AUTHORIZATION) AuthHeader authHeader, PasswordUpdateData passwordUpdateData);

    @POST
    @Path("/")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    User createUser(@HeaderParam(AUTHORIZATION) AuthHeader authHeader, UserData userData);

    @POST
    @Path("/register")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    User registerUser(UserRegistrationData userRegistrationData);

    @POST
    @Path("/activate/{emailCode}")
    void activateUser(@PathParam("emailCode") String emailCode);

    @PUT
    @Path("/{userJid}")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    User updateUser(
            @HeaderParam(AUTHORIZATION) AuthHeader authHeader,
            @PathParam("userJid") String userJid,
            UserData userData);

    @GET
    @Path("/{userJid}/profile")
    @Produces(APPLICATION_JSON)
    UserProfile getUserProfile(@HeaderParam(AUTHORIZATION) AuthHeader authHeader, @PathParam("userJid") String userJid);

    @PUT
    @Path("/{userJid}/profile")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    UserProfile updateUserProfile(
            @HeaderParam(AUTHORIZATION) AuthHeader authHeader,
            @PathParam("userJid") String userJid,
            UserProfile userProfile);

    @DELETE
    @Path("/{userJid}/avatar")
    void deleteUserAvatar(@HeaderParam(AUTHORIZATION) AuthHeader authHeader, @PathParam("userJid") String userJid);

    @POST
    @Path("/request-reset-password/{email}")
    void requestToResetUserPassword(@PathParam("email") String email);

    @POST
    @Path("/reset-password")
    @Consumes(APPLICATION_JSON)
    void resetUserPassword(PasswordResetData passwordResetData);

    @POST
    @Path("/usernames")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    Map<String, String> findUsernamesByJids(Set<String> jids);
}
