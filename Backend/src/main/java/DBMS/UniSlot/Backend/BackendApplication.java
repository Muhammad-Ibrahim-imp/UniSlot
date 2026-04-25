package DBMS.UniSlot.Backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * ============================================================
 * DBMS.UniSlot.Backend — Entry Point
 * ============================================================
 *
 * University Lecture Slot Selection System
 *
 * This application allows:
 *  - Admins to configure departments, degrees, courses,
 *    professors and their time slots.
 *  - Students (who have paid fees) to pick lecture slots
 *    for each of their courses.
 *  - PDF generation of a student's weekly timetable.
 *
 * @EnableJpaAuditing enables automatic population of
 * @CreatedDate / @LastModifiedDate fields on entities.
 * ============================================================
 */
@SpringBootApplication
@EnableJpaAuditing
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}
}