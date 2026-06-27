package by.iivanov.rpm.iam.user.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.Repository;

interface SpringDataUserSummaryRepository extends Repository<UserSummaryView, UUID> {

    List<UserSummaryView> findByIdNot(UUID id, Sort sort);
}
