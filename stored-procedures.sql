-- Smart Clinic Management System
-- Stored procedures for daily and summary reporting.
-- Run against the `cms` MySQL database created for this project.

-- =====================================================================
-- Daily Appointment Report by Doctor
-- Lists all appointments on a given date, grouped by doctor, with
-- appointment time/status and the patient's name and phone number.
-- Useful for daily operational reviews in the clinic.
-- =====================================================================
DELIMITER $

CREATE PROCEDURE GetDailyAppointmentReportByDoctor(
    IN report_date DATE
)
BEGIN
    SELECT
        d.name AS doctor_name,
        a.appointment_time,
        a.status,
        p.name AS patient_name,
        p.phone AS patient_phone
    FROM
        appointment a
    JOIN
        doctor d ON a.doctor_id = d.id
    JOIN
        patient p ON a.patient_id = p.id
    WHERE
        DATE(a.appointment_time) = report_date
    ORDER BY
        d.name, a.appointment_time;
END $

DELIMITER ;

-- Example:
-- CALL GetDailyAppointmentReportByDoctor('2025-04-15');


-- =====================================================================
-- Doctor with Most Patients By Month
-- Identifies the doctor who saw the most patients in a given month and
-- year. Helps clinic managers understand which doctor had the highest
-- patient load during a time period.
-- =====================================================================
DELIMITER $

CREATE PROCEDURE GetDoctorWithMostPatientsByMonth(
    IN input_month INT,
    IN input_year INT
)
BEGIN
    SELECT
        doctor_id,
        COUNT(patient_id) AS patients_seen
    FROM
        appointment
    WHERE
        MONTH(appointment_time) = input_month
        AND YEAR(appointment_time) = input_year
    GROUP BY
        doctor_id
    ORDER BY
        patients_seen DESC
    LIMIT 1;
END $

DELIMITER ;

-- Example:
-- CALL GetDoctorWithMostPatientsByMonth(4, 2025);


-- =====================================================================
-- Doctor with Most Patients by Year
-- Identifies the doctor with the most patients in a given year. Useful
-- for generating annual performance summaries.
-- =====================================================================
DELIMITER $

CREATE PROCEDURE GetDoctorWithMostPatientsByYear(
    IN input_year INT
)
BEGIN
    SELECT
        doctor_id,
        COUNT(patient_id) AS patients_seen
    FROM
        appointment
    WHERE
        YEAR(appointment_time) = input_year
    GROUP BY
        doctor_id
    ORDER BY
        patients_seen DESC
    LIMIT 1;
END $

DELIMITER ;

-- Example:
-- CALL GetDoctorWithMostPatientsByYear(2025);
