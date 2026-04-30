package by.iivanov.rpm.iam.user.domain;

import java.util.UUID;

public final class JtiGenerator {

    public static String generate() {
        return UUID.randomUUID().toString();
    }

    private JtiGenerator() {}
}
