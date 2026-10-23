ALTER TABLE skills
  ALTER COLUMN proficiency_level TYPE INTEGER
    USING proficiency_level::INTEGER,
  ALTER COLUMN target_level TYPE INTEGER
    USING target_level::INTEGER;
