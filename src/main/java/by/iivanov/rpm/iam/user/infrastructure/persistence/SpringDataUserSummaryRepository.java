package by.iivanov.rpm.iam.user.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.Repository;

interface SpringDataUserSummaryRepository extends Repository<UserSummaryView, UUID> {

    @EntityGraph(attributePaths = {"createdBy", "updatedBy"})
    List<UserSummaryView> findByIdNot(UUID id, Sort sort);
}
