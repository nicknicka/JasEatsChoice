import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenerateAdminPassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "admin123";
        String encoded = encoder.encode(password);
        System.out.println("Plain password: " + password);
        System.out.println("Encoded password: " + encoded);
    }
}
