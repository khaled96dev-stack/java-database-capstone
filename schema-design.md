# Smart Clinic System Schema Design

This schema is designed for a Spring Boot + MySQL + MongoDB Smart Clinic System that manages patient records, doctor schedules, appointments, administrative operations, and clinical document data.

## 1) MySQL Database Design

### Overview
The relational database stores structured, transactional data such as patient profiles, doctor profiles, appointment bookings, and administrative users. MySQL is suited for strong consistency, referential integrity, and efficient querying of tabular records.

### Table: `patients`

```sql
CREATE TABLE patients (
    patient_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender ENUM('MALE', 'FEMALE', 'OTHER') NOT NULL,
    address TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (patient_id),
    UNIQUE KEY uq_patients_email (email),
    UNIQUE KEY uq_patients_phone (phone),
    CHECK (first_name <> ''),
    CHECK (last_name <> '')
) ENGINE=InnoDB;
```

- `patient_id`: primary key for unique patient identification.
- `email` and `phone`: unique to avoid duplicate patient records.
- `created_at` / `updated_at`: audit trail fields for synchronization and reporting.

### Table: `doctors`

```sql
CREATE TABLE doctors (
    doctor_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    specialty VARCHAR(100) NOT NULL,
    license_number VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    status ENUM('ACTIVE', 'ON_LEAVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (doctor_id),
    UNIQUE KEY uq_doctors_license_number (license_number),
    UNIQUE KEY uq_doctors_email (email),
    CHECK (specialty <> ''),
    CHECK (license_number <> '')
) ENGINE=InnoDB;
```

- `doctor_id`: primary key for each physician.
- `license_number`: unique to ensure legal/credential validation.
- `status`: supports clinician availability tracking.

### Table: `appointments`

```sql
CREATE TABLE appointments (
    appointment_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    patient_id BIGINT UNSIGNED NOT NULL,
    doctor_id BIGINT UNSIGNED NOT NULL,
    appointment_date DATE NOT NULL,
    appointment_time TIME NOT NULL,
    duration_minutes INT NOT NULL DEFAULT 30,
    status ENUM('SCHEDULED', 'CONFIRMED', 'COMPLETED', 'CANCELLED', 'NO_SHOW') NOT NULL DEFAULT 'SCHEDULED',
    notes TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (appointment_id),
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    UNIQUE KEY uq_appointment_slot (doctor_id, appointment_date, appointment_time),
    CHECK (duration_minutes > 0),
    CHECK (appointment_time BETWEEN '00:00:00' AND '23:59:59')
) ENGINE=InnoDB;
```

- `patient_id` and `doctor_id`: foreign keys linking appointments to the patient and doctor tables.
- `uq_appointment_slot`: prevents double-booking the same doctor at the same time.
- `status`: tracks the lifecycle of each appointment.

### Table: `admins`

```sql
CREATE TABLE admins (
    admin_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    role ENUM('SUPER_ADMIN', 'CLINIC_ADMIN', 'RECEPTION') NOT NULL DEFAULT 'RECEPTION',
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (admin_id),
    UNIQUE KEY uq_admins_username (username),
    UNIQUE KEY uq_admins_email (email),
    CHECK (username <> ''),
    CHECK (full_name <> '')
) ENGINE=InnoDB;
```

- `admin_id`: primary key for system administrators.
- `role`: distinguishes access levels for operations such as appointment approvals and staff management.
- `is_active`: supports temporary permission disabling without deleting records.

### Optional Related Table: `medical_records`

```sql
CREATE TABLE medical_records (
    record_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    patient_id BIGINT UNSIGNED NOT NULL,
    doctor_id BIGINT UNSIGNED NOT NULL,
    diagnosis TEXT NOT NULL,
    treatment_plan TEXT NOT NULL,
    notes TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (record_id),
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CHECK (diagnosis <> ''),
    CHECK (treatment_plan <> '')
) ENGINE=InnoDB;
```

This table is often useful in a clinic system because patient health information is often retrieved by patient and doctor context. It also keeps treatment notes separate from appointment scheduling data.

### Design Justification

- Structured relational tables are used for core operational records that require consistency, validation, and controlled updates.
- Foreign keys preserve data integrity between patients, doctors, appointments, and administration.
- Unique constraints prevent duplicate identities and scheduling conflicts.
- `ENUM` fields are used for status values to reduce invalid data entries and improve reporting clarity.

---

## 2) MongoDB Collection Design

### Collection: `prescriptions`

MongoDB is best suited for clinical document records where each prescription may contain varying nested data such as medications, instructions, patient-specific notes, and dosage history. This collection stores document-oriented information that is not always well represented in a rigid relational schema.

```json
{
  "_id": "pres_1001",
  "patientId": 1024,
  "doctorId": 17,
  "visitDate": "2026-10-20",
  "clinic": {
    "name": "Smart Clinic Center",
    "branch": "Downtown",
    "location": "Al-Khobar"
  },
  "medications": [
    {
      "name": "Amoxicillin",
      "dosage": "500mg",
      "frequency": "Twice daily",
      "durationDays": 7,
      "instructions": "Take after food",
      "status": "active"
    },
    {
      "name": "Paracetamol",
      "dosage": "650mg",
      "frequency": "As needed",
      "durationDays": 3,
      "instructions": "Use only for fever or pain",
      "status": "active"
    }
  ],
  "symptoms": ["Fever", "Sore throat", "Cough"],
  "diagnosis": "Upper respiratory infection",
  "followUp": {
    "recommended": true,
    "date": "2026-10-27",
    "reason": "Reassessment of symptoms"
  },
  "createdBy": "dr_17",
  "createdAt": "2026-10-20T09:15:00Z",
  "updatedAt": "2026-10-20T09:15:00Z",
  "allergies": ["Penicillin"],
  "notes": "Patient advised to avoid self-medicating and report any rash."
}
```

### Why this design works

- `medications` is an array because a single prescription can contain multiple drugs.
- `clinic` is nested to keep address and branch information with the prescription document.
- `followUp` is embedded as a structured object for actionable clinical instructions.
- `allergies` and `symptoms` are arrays because they can vary by patient and visit.
- `createdAt` and `updatedAt` make it easy to track document history and audit changes.

### Additional MongoDB Collections

Other useful collections may include:

- `feedback`: stores patient satisfaction comments and ratings.
- `system_logs`: writes timestamped operational and security events for auditing.
- `medical_images`: stores references to uploaded clinical documents or X-rays.

---

## 3) Recommended Hybrid Approach

- Use MySQL for relational records that need joins, strict validation, and transactional consistency.
- Use MongoDB for flexible clinical documents, audit logs, and rich nested information such as prescriptions and feedback.
- Keep a mapped relationship between systems by storing MySQL IDs and MongoDB document references for traceability.

This hybrid approach gives the Smart Clinic System both the consistency of a relational database and the flexibility of document-oriented storage for healthcare content.


## 4) Summary of Data Managed by the System

The Smart Clinic System must manage:

- Patient personal and medical information
- Doctor profiles and specialties
- Appointments, scheduling, and cancellations
- Administrative staff and access roles
- Clinical assessments and treatment plans
- Prescription details and follow-up instructions
- Patient feedback and satisfaction records
- System logs and operational audit trails

This combination ensures the application can support both day-to-day clinic operations and long-term healthcare data management.
