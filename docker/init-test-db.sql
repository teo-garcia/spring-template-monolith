SELECT 'CREATE DATABASE spring_monolith_test'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'spring_monolith_test')\gexec
