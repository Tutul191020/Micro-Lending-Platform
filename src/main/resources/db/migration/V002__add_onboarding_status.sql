-- Migration: Add onboarding status tracking to users table
-- Description: Adds onboarding_status and onboarding_completed_at columns to track user document completion
-- Author: QarzMukti Team
-- Date: 2025-11-29

-- Add onboarding_status column to users table
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS onboarding_status VARCHAR(30) NOT NULL DEFAULT 'REGISTERED';

-- Add onboarding_completed_at column
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS onboarding_completed_at TIMESTAMP WITH TIME ZONE;

-- Create index for faster queries on onboarding_status
CREATE INDEX IF NOT EXISTS idx_users_onboarding_status ON users(onboarding_status);

-- Update existing users based on their current state
-- Users who are already approved should be marked as onboarding completed
UPDATE users 
SET onboarding_status = 'ONBOARDING_COMPLETED',
    onboarding_completed_at = verified_at
WHERE verification_status = 'APPROVED' AND verified = true
  AND onboarding_status = 'REGISTERED';

-- Users with all documents uploaded should be marked as documents completed
UPDATE users 
SET onboarding_status = 'ONBOARDING_COMPLETED',
    onboarding_completed_at = CURRENT_TIMESTAMP
WHERE verification_status = 'PENDING' 
  AND id IN (
    SELECT user_id FROM user_documents 
    WHERE national_id_document_path IS NOT NULL 
      AND electricity_bill_document_path IS NOT NULL 
      AND profile_photo_path IS NOT NULL
  )
  AND onboarding_status = 'REGISTERED';

-- Users with at least one document uploaded should be marked as documents pending
UPDATE users 
SET onboarding_status = 'DOCUMENTS_PENDING'
WHERE verification_status = 'PENDING' 
  AND id IN (
    SELECT user_id FROM user_documents 
    WHERE national_id_document_path IS NOT NULL 
       OR electricity_bill_document_path IS NOT NULL 
       OR profile_photo_path IS NOT NULL
  )
  AND onboarding_status = 'REGISTERED';

-- Add comment to the column for documentation
COMMENT ON COLUMN users.onboarding_status IS 'Tracks user onboarding progress: REGISTERED, DOCUMENTS_PENDING, DOCUMENTS_COMPLETED, ONBOARDING_COMPLETED';
COMMENT ON COLUMN users.onboarding_completed_at IS 'Timestamp when user completed onboarding (all documents uploaded)';
