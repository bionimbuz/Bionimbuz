package br.unb.cic.bionimbus.rest.resource;

import br.unb.cic.bionimbus.controller.jobcontroller.JobController;
import br.unb.cic.bionimbus.controller.usercontroller.UserController;
import br.unb.cic.bionimbus.persistence.dao.FileDao;
import br.unb.cic.bionimbus.persistence.dao.UserDao;
import br.unb.cic.bionimbus.rest.model.UploadedFileInfo;
import br.unb.cic.bionimbus.rest.model.User;
import java.util.Calendar;
import java.util.List;

import javax.persistence.NoResultException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import br.unb.cic.bionimbus.rest.request.LoginRequest;
import br.unb.cic.bionimbus.rest.request.LogoutRequest;
import br.unb.cic.bionimbus.rest.request.RequestInfo;
import br.unb.cic.bionimbus.rest.request.SignUpRequest;
import br.unb.cic.bionimbus.rest.response.LogoutResponse;
import br.unb.cic.bionimbus.rest.response.ResponseInfo;
import br.unb.cic.bionimbus.rest.response.SignUpResponse;
import javax.annotation.security.PermitAll;
import javax.ws.rs.core.Response;

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

        LOGGER.info("Login request received: [login: " + requestUser.getLogin() + ", password: " + requestUser.getPassword().charAt(0) + "*****]");

        // Verifies if the request user exists on database
        User responseUser = null;

        try {
            responseUser = userDao.findByLogin(requestUser.getLogin());
        } catch (NoResultException e) {
            LOGGER.info("User " + requestUser.getLogin() + " not found");
            
            // Returns to Client
            return Response.status(200).entity(loginRequest.getUser()).build();
        } catch (Exception e) {
            LOGGER.error("[Exception] " + e.getMessage());
        }

        // Verifies if the user from database is null and the password is right
        if (responseUser != null && (requestUser.getPassword().equals(responseUser.getPassword()))) {
            List<UploadedFileInfo> userFiles = fileInfoDao.listByUserId(responseUser.getId());
            responseUser.setFiles(userFiles);

            // Encrypts secretKey with bCrypt encoder
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            String secretKey = encoder.encode(requestUser.getLogin() + Calendar.getInstance());

            responseUser.setSecurityToken(secretKey);

            // Logs user in ZooKeeper structure
            userController.logUser(responseUser.getLogin());
            
            LOGGER.info("Children count: " + userController.getLoggedUsersCount());
            
            // Sets response populated user
            return Response.status(200).entity(responseUser).build();
        } else {

            // Else, returns the same user
            return Response.status(200).entity(loginRequest.getUser()).build();
        }
    }

    @POST
    @Path("/logout")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public LogoutResponse logout(LogoutRequest logoutRequest) {
        LOGGER.info("Logout request received: [login: " + logoutRequest.getUser().getLogin() + "]");

        LogoutResponse response = new LogoutResponse();

        // Inform ZooKeeper of the logout
        userController.logoutUser(logoutRequest.getUser().getLogin());
        
        // Set that logout was successful
        response.setLogoutSuccess(true);

        return response;
    }

    @POST
    @Path("/signup")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public SignUpResponse signUp(SignUpRequest request) {
        LOGGER.info("Sign up request received. [login: " + request.getUser().getLogin() + "]");

        userDao.exists(request.getUser().getLogin());

        SignUpResponse response = new SignUpResponse();
        response.setAdded(true);

        return response;
    }

    @Override
    public ResponseInfo handleIncoming(RequestInfo request) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
