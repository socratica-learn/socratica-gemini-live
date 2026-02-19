// MongoDB initialization script
// This script runs when MongoDB container starts for the first time

// Switch to the socratica_dev database
db = db.getSiblingDB('socratica_dev');

// Create the user for the database (if not exists)
db.createUser({
  user: 'socratica_user',
  pwd: 'socratica_password',
  roles: [
    {
      role: 'readWrite',
      db: 'socratica_dev'
    }
  ]
});

// Note: Collections (like 'users') are created automatically when first document is inserted
// Indexes will be created by Spring Data MongoDB based on @Indexed annotations

print('MongoDB initialization completed!');
print('Database: socratica_dev');
print('User: socratica_user');
print('Collections will be created automatically when data is inserted.');




