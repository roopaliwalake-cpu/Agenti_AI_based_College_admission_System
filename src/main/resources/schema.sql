CREATE TABLE IF NOT EXISTS students (
    student_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(255) NOT NULL,
    date_of_birth DATE,
    address VARCHAR(1000),
    degree VARCHAR(255) NOT NULL,
    university VARCHAR(255),
    percentage DOUBLE NOT NULL
);

CREATE TABLE IF NOT EXISTS student_accounts (
    account_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at DATETIME
);

CREATE TABLE IF NOT EXISTS admissions (
    admission_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    course_name VARCHAR(255) NOT NULL,
    college_name VARCHAR(255) NOT NULL,
    college_type VARCHAR(255) NOT NULL,
    fees DECIMAL(12, 2),
    duration VARCHAR(255),
    admission_date DATETIME,
    status VARCHAR(255),
    college_source VARCHAR(1200),
    CONSTRAINT fk_admission_student FOREIGN KEY (student_id) REFERENCES students(student_id)
);

CREATE TABLE IF NOT EXISTS payments (
    payment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    admission_id BIGINT NOT NULL,
    amount DECIMAL(12, 2),
    transaction_id VARCHAR(255) NOT NULL,
    payment_date DATETIME,
    payment_status VARCHAR(255),
    CONSTRAINT fk_payment_admission FOREIGN KEY (admission_id) REFERENCES admissions(admission_id)
);

CREATE TABLE IF NOT EXISTS documents (
    document_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    document_type VARCHAR(255),
    document_path VARCHAR(255),
    upload_date DATETIME,
    CONSTRAINT fk_document_student FOREIGN KEY (student_id) REFERENCES students(student_id)
);

CREATE TABLE IF NOT EXISTS policy_acceptances (
    acceptance_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    policy_id VARCHAR(255),
    accepted_date DATETIME,
    status VARCHAR(255),
    CONSTRAINT fk_policy_student FOREIGN KEY (student_id) REFERENCES students(student_id)
);

CREATE TABLE IF NOT EXISTS merit_list_entries (
    merit_list_entry_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    admission_id BIGINT NOT NULL,
    course_name VARCHAR(255),
    rank_position INT,
    selected BOOLEAN,
    released_at DATETIME,
    CONSTRAINT fk_merit_admission FOREIGN KEY (admission_id) REFERENCES admissions(admission_id)
);
