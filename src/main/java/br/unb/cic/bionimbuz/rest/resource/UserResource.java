package br.unb.cic.bionimbuz.rest.resource;

import java.util.Calendar;
import java.util.List;

import javax.annotation.security.PermitAll;
import javax.persistence.NoResultException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import br.unb.cic.bionimbuz.controller.jobcontroller.JobController;
import br.unb.cic.bionimbuz.controller.usercontroller.UserController;
import br.unb.cic.bionimbuz.model.FileInfo;
import br.unb.cic.bionimbuz.model.User;
import br.unb.cic.bionimbuz.persistence.dao.FileDao;
import br.unb.cic.bionimbuz.persistence.dao.UserDao;
import br.unb.cic.bionimbuz.persistence.dao.WorkflowDao;
import br.unb.cic.bionimbuz.rest.request.LoginRequest;
import br.unb.cic.bionimbuz.rest.request.LogoutRequest;
import br.unb.cic.bionimbuz.rest.request.RequestInfo;
import br.unb.cic.bionimbuz.rest.request.SignUpRequest;
import br.unb.cic.bionimbuz.rest.response.ResponseInfo;
import br.unb.cic.bionimbuz.rest.response.SignUpResponse;

@Path("/rest")
public class UserResource extends AbstractResource {

    private final UserDao userDao;
    private final FileDao fileInfoDao;

    public UserResource(JobController jobController, UserController userController) {
        this.jobController = jobController;
        this.userDao = new UserDao();
        this.fileInfoDao = new FileDao();
        this.userController = userController;
    }

    @PermitAll
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(LoginRequest loginRequest) {
        User requestUser = loginRequest.getUser();

        LOGGER.info("Login request received: [login: " + requestUser.getLogin() + "]");

        // Verifies if the request user exists on database
        User user = null;
        try {
            user = userDao.findByLogin(requestUser.getLogin());
            if (user != null) {
                user.setFiles(new FileDao().listByUserId(user.getId()));
                user.setWorkflows(new WorkflowDao().listByUserId(user.getId()));
            }
        } catch (NoResultException e) {
            LOGGER.info("User " + requestUser.getLogin() + " not found");

            // Returns to Client
            return Response.status(200).entity(loginRequest.getUser()).build();
        } catch (Exception e) {
            LOGGER.error("[Exception] " + e.getMessage());
        }

        // Verifies if the user from database is null and the password is right
        if (user != null) {
            List<FileInfo> userFiles = fileInfoDao.listByUserId(user.getId());
            user.setFiles(userFiles);

            // Encrypts secretKey with bCrypt encoder
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            String secretKey = encoder.encode(requestUser.getLogin() + Calendar.getInstance());

            user.setSecurityToken(secretKey);

            // Logs user in ZooKeeper structure
            userController.logUser(user.getLogin());

            LOGGER.info("Children count: " + userController.getLoggedUsersCount());

            // Sets response populated user
            return Response.status(200).entity(user).build();
        } else {

            // Else, returns the same user
            return Response.status(200).entity(loginRequest.getUser()).build();
        }
    }

    @POST
    @Path("/logout")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response logout(LogoutRequest logoutRequest) {
        LOGGER.info("Logout request received: [login: " + logoutRequest.getUser().getLogin() + "]");

        // Inform ZooKeeper of the logout
        userController.logoutUser(logoutRequest.getUser().getLogin());

        // Set that logout was successful
        return Response.status(200).entity(true).build();
    }

    @POST
    @Path("/signup")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public SignUpResponse signUp(SignUpRequest request) {
        LOGGER.info("Sign up request received. [login: " + request.getUser().getLogin() + "]");
        User user = request.getUser();

        try {
            // Verifies if user exists
            if (!userDao.exists(user.getLogin())) {
                // If not, persists it
                userDao.persist(user);

                // Creates positive response
                SignUpResponse response = new SignUpResponse();
                response.setAdded(true);

                // Return to application
                return response;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Creates negative response
        SignUpResponse response = new SignUpResponse();
        response.setAdded(false);

        // Return to application
        return response;

    }

    @Override
    public ResponseInfo handleIncoming(RequestInfo request) {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose Tools | Templates.
    }
}
