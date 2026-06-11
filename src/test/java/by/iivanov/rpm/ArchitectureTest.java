package by.iivanov.rpm;

import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.annotatedWith;
import static com.tngtech.archunit.lang.conditions.ArchConditions.be;
import static com.tngtech.archunit.lang.conditions.ArchPredicates.is;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaPackage;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import java.lang.annotation.Annotation;
import org.jmolecules.archunit.JMoleculesArchitectureRules;
import org.jmolecules.archunit.JMoleculesDddRules;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

@AnalyzeClasses(packagesOf = RpmDddApplication.class, importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule dddRules = JMoleculesDddRules.all();

    @ArchTest
    static final ArchRule onion = JMoleculesArchitectureRules.ensureOnionSimple();

    @ArchTest
    static final ArchRule classesShouldBeNullSafe =
            classes().should(be(nullSafe())).because("every class must be null safe");

    /**
     * Evaluates the predicate on the class's package.
     */
    static DescribedPredicate<JavaClass> resideInPackageThat(DescribedPredicate<? super JavaPackage> condition) {
        return condition
                .onResultOf(JavaClass.Functions.GET_PACKAGE)
                .as("reside in a package that %s", condition.getDescription());
    }

    /**
     * Checks if the class's package is annotated with a specific annotation.
     */
    static DescribedPredicate<JavaClass> resideInPackageAnnotatedWith(Class<? extends Annotation> annotationClass) {
        return resideInPackageThat(is(annotatedWith(annotationClass)));
    }

    private static DescribedPredicate<JavaClass> nullSafe() {
        return resideInPackageAnnotatedWith(NullMarked.class)
                .or(annotatedWith(NullMarked.class))
                .as("null safe (reside in a @NullMarked package or be annotated with @NullMarked)");
    }

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
