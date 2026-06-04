CREATE TABLE students (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    grade VARCHAR(40) NOT NULL,
    contact_number VARCHAR(30),
    address VARCHAR(255),
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE book_issues (
    id BIGINT NOT NULL AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    issue_date DATE NOT NULL,
    book_title VARCHAR(160) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    discount DECIMAL(12,2) NOT NULL,
    total_due DECIMAL(12,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    remarks VARCHAR(255),
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_book_issues_student FOREIGN KEY (student_id) REFERENCES students (id)
);

CREATE INDEX idx_book_issues_issue_date ON book_issues (issue_date);
CREATE INDEX idx_book_issues_status ON book_issues (status);

CREATE TABLE payments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    book_issue_id BIGINT NOT NULL,
    receipt_number VARCHAR(40) NOT NULL,
    payment_date DATE NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    payment_mode VARCHAR(20) NOT NULL,
    notes VARCHAR(255),
    created_by VARCHAR(80) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_payments_receipt_number UNIQUE (receipt_number),
    CONSTRAINT fk_payments_book_issue FOREIGN KEY (book_issue_id) REFERENCES book_issues (id)
);

CREATE INDEX idx_payments_payment_date ON payments (payment_date);
