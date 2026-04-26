package by.iivanov.rpm;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.jmolecules.archunit.JMoleculesArchitectureRules;
import org.jmolecules.archunit.JMoleculesDddRules;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

@AnalyzeClasses(packagesOf = RpmDddApplication.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule dddRules = JMoleculesDddRules.all();

    @ArchTest
    static final ArchRule onion = JMoleculesArchitectureRules.ensureOnionSimple();

    static final ApplicationModules modules = ApplicationModules.of(RpmDddApplication.class);

    @Test
    void verifyModulithModules() {
        modules.verify();
    }

    @Test
    void createModulesDocumentation() {
        new Documenter(modules).writeDocumentation();
    }
}
