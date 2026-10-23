CREATE TABLE users (
    id UUID PRIMARY KEY,
    name VARCHAR(80) NOT NULL,
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT users_email_unique UNIQUE (email)
);

CREATE TABLE dsa_problems (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    topic VARCHAR(80) NOT NULL,
    difficulty VARCHAR(12) NOT NULL CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD')),
    platform VARCHAR(80) NOT NULL,
    problem_url VARCHAR(2048),
    status VARCHAR(16) NOT NULL CHECK (status IN ('TODO', 'IN_PROGRESS', 'SOLVED')),
    notes TEXT,
    solved_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX dsa_problems_user_created_idx ON dsa_problems(user_id, created_at DESC);
CREATE INDEX dsa_problems_user_status_idx ON dsa_problems(user_id, status);
CREATE INDEX dsa_problems_user_topic_idx ON dsa_problems(user_id, topic);

CREATE TABLE projects (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(160) NOT NULL,
    description TEXT,
    status VARCHAR(16) NOT NULL CHECK (status IN ('PLANNED', 'IN_PROGRESS', 'COMPLETED')),
    repository_url VARCHAR(2048),
    demo_url VARCHAR(2048),
    start_date DATE,
    target_date DATE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX projects_user_created_idx ON projects(user_id, created_at DESC);
CREATE INDEX projects_user_status_idx ON projects(user_id, status);

CREATE TABLE project_technologies (
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    technology VARCHAR(40) NOT NULL,
    PRIMARY KEY (project_id, technology)
);

CREATE TABLE skills (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(80) NOT NULL,
    proficiency_level SMALLINT NOT NULL CHECK (proficiency_level BETWEEN 1 AND 5),
    target_level SMALLINT NOT NULL CHECK (target_level BETWEEN 1 AND 5),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT skills_user_name_unique UNIQUE (user_id, name)
);
CREATE INDEX skills_user_category_idx ON skills(user_id, category);

CREATE TABLE activity_logs (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    action_type VARCHAR(32) NOT NULL,
    entity_type VARCHAR(32) NOT NULL,
    entity_id UUID,
    created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX activity_logs_user_created_idx ON activity_logs(user_id, created_at DESC);
