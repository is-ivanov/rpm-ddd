package by.iivanov.rpm.testing.instancio;

import by.iivanov.rpm.iam.user.domain.EmailAddress;
import java.util.Map;
import org.instancio.Instancio;
import org.instancio.Node;
import org.instancio.Random;
import org.instancio.generator.Generator;
import org.instancio.generators.Generators;
import org.instancio.spi.InstancioServiceProvider;

public class IamDomainProvider implements InstancioServiceProvider {

    @Override
    public GeneratorProvider getGeneratorProvider() {

        Map<Class<?>, Generator<?>> generators = Map.of(EmailAddress.class, new EmailAddressGenerator());

        return (Node node, Generators _) -> generators.get(node.getTargetClass());
    }

    private static class EmailAddressGenerator implements Generator<EmailAddress> {

        @Override
        public EmailAddress generate(Random random) {
            return new EmailAddress(Instancio.gen().net().email().get());
        }
    }
}
