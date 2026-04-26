package by.iivanov.rpm.shared.infrastructure;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.jmolecules.architecture.onion.simplified.ApplicationRing;
import org.springframework.stereotype.Service;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@ApplicationRing
@Service
public @interface ApplicationService {}
