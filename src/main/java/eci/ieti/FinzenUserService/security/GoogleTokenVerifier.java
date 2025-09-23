package eci.ieti.FinzenUserService.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.Optional;

@Component
public class GoogleTokenVerifier {
    
    @Value("${google.client.id}")
    private String client_id;

    public Optional<GoogleIdToken.Payload> verify(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(client_id))
                    .build();
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                return Optional.of(idToken.getPayload());
            }
        } catch (Exception e) {
            return Optional.empty();
        }
        return Optional.empty();
    }
}