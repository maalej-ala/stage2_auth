//package controller;
//
//import entity.User;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@SpringBootTest(classes = stage2.authentification.AuthentificationApplication.class)
//@AutoConfigureMockMvc(addFilters = false)
//public class UserControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Test
//    public void testCreateUser() throws Exception {
//        mockMvc.perform(post("/api/users/register")
//        		.with(csrf())
//                .contentType(MediaType.APPLICATION_JSON)
//                .content("""
//                    {
//                      "username": "john_doe",
//                      "email": "john@example.com",
//                      "password": "password123"
//                    }
//                """))
//                .andExpect(status().isOk()); // or .isCreated() if you return 201
//    }
//}
