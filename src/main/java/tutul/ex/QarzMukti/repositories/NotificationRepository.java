package tutul.ex.QarzMukti.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tutul.ex.QarzMukti.entities.Notification;

import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {}