package eci.ieti.FinzenUserService.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;

@Component
public class GoogleTokenVerifier {

    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenVerifier(
            @Value("${google.webClientId}") String webClientId,
            @Value("${google.androidClientId}") String androidClientId
    ) {
        this.verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                // Usamos GsonFactory aquí
                new GsonFactory()
        )
                .setAudience(Arrays.asList(webClientId, androidClientId))
                .build();
    }

    /**
     * Verifica el idToken recibido desde frontend.
     *
     * @param idTokenString token que llega desde GoogleSignIn
     * @return GoogleIdToken si es válido, null en caso contrario
     */
    public GoogleIdToken.Payload verify(String idTokenString) throws GeneralSecurityException, IOException {
        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken != null) {
            return idToken.getPayload();
        } else {
            return null;
        }
    }
}