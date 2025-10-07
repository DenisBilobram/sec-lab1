package sec.lab1.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import sec.lab1.app.model.LabPost;

public interface LabPostRepository extends JpaRepository<LabPost, Long> {}