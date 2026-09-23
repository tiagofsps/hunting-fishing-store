-- Phase 4 (Steps 23/25): PSP gateway fields on payments.
-- Stub gateway result fields; nullable except nothing is required after initiation.
alter table payments
    add column multibanco_entity varchar(255),
    add column multibanco_reference varchar(255) unique,
    add column mbway_phone varchar(255),
    add column payment_url varchar(255),
    add column reference_expires_at timestamp(6);