-- Create media schema for media-service
CREATE SCHEMA IF NOT EXISTS media;

-- Create music genres table
CREATE TABLE media.music_genres (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create ad types table
CREATE TABLE media.ad_types (
    id BIGSERIAL PRIMARY KEY,
    tenant_id UUID,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    is_selectable BOOLEAN DEFAULT true,
    can_play_remotely BOOLEAN DEFAULT true,
    can_play_individually BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create music files table
CREATE TABLE media.music (
    id BIGSERIAL PRIMARY KEY,
    tenant_id UUID NOT NULL,
    genre_id BIGINT REFERENCES media.music_genres(id),
    title VARCHAR(255) NOT NULL,
    artist VARCHAR(255),
    album VARCHAR(255),
    duration_seconds INTEGER,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    file_format VARCHAR(20),
    bitrate INTEGER,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create advertisement files table
CREATE TABLE media.advertisements (
    id BIGSERIAL PRIMARY KEY,
    tenant_id UUID NOT NULL,
    ad_type_id BIGINT REFERENCES media.ad_types(id),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    duration_seconds INTEGER,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    file_format VARCHAR(20),
    target_audience VARCHAR(100),
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_media_music_tenant_id ON media.music(tenant_id);
CREATE INDEX idx_media_music_genre_id ON media.music(genre_id);
CREATE INDEX idx_media_music_title ON media.music(title);
CREATE INDEX idx_media_music_artist ON media.music(artist);
CREATE INDEX idx_media_music_created_at ON media.music(created_at);

CREATE INDEX idx_media_advertisements_tenant_id ON media.advertisements(tenant_id);
CREATE INDEX idx_media_advertisements_ad_type_id ON media.advertisements(ad_type_id);
CREATE INDEX idx_media_advertisements_name ON media.advertisements(name);
CREATE INDEX idx_media_advertisements_created_at ON media.advertisements(created_at);

CREATE INDEX idx_media_ad_types_tenant_id ON media.ad_types(tenant_id);
CREATE INDEX idx_media_ad_types_name ON media.ad_types(name);

-- Insert default music genres
INSERT INTO media.music_genres (name, description) VALUES
('Pop', 'Popular music'),
('Rock', 'Rock music'),
('Jazz', 'Jazz music'),
('Classical', 'Classical music'),
('Electronic', 'Electronic music'),
('Hip Hop', 'Hip hop music'),
('Country', 'Country music'),
('Blues', 'Blues music'),
('Folk', 'Folk music'),
('R&B', 'Rhythm and blues');

-- Insert default ad types
INSERT INTO media.ad_types (tenant_id, name, description) VALUES
(NULL, 'General', 'General advertisements'),
(NULL, 'Promotional', 'Promotional content'),
(NULL, 'Announcement', 'Public announcements'),
(NULL, 'Emergency', 'Emergency broadcasts');
