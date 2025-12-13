-- Default Contexts (run only if not exists)
INSERT INTO contexts (name, description, color, icon, is_default, created_at, updated_at) 
SELECT '@home', 'Tasks to do at home', '#4CAF50', 'home', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM contexts WHERE name = '@home' AND is_default = true);

INSERT INTO contexts (name, description, color, icon, is_default, created_at, updated_at) 
SELECT '@office', 'Tasks to do at work', '#2196F3', 'business', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM contexts WHERE name = '@office' AND is_default = true);

INSERT INTO contexts (name, description, color, icon, is_default, created_at, updated_at) 
SELECT '@phone', 'Tasks requiring phone calls', '#FF9800', 'phone', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM contexts WHERE name = '@phone' AND is_default = true);

INSERT INTO contexts (name, description, color, icon, is_default, created_at, updated_at) 
SELECT '@errands', 'Tasks requiring going out', '#9C27B0', 'directions_car', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM contexts WHERE name = '@errands' AND is_default = true);

INSERT INTO contexts (name, description, color, icon, is_default, created_at, updated_at) 
SELECT '@computer', 'Tasks requiring computer', '#607D8B', 'computer', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM contexts WHERE name = '@computer' AND is_default = true);

INSERT INTO contexts (name, description, color, icon, is_default, created_at, updated_at) 
SELECT '@waiting', 'Waiting for someone/something', '#795548', 'hourglass_empty', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM contexts WHERE name = '@waiting' AND is_default = true);
