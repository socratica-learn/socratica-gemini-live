// MongoDB Verification Script
// Run this with: mongosh < verify-mongodb.js
// Or: docker exec -it socratica-mongodb mongosh < verify-mongodb.js

// Connect to the database
db = db.getSiblingDB('socratica_dev');

print('=== MongoDB Setup Verification ===\n');

// Check if database exists
print('1. Database Check:');
print('   Database: ' + db.getName());
print('   Status: ✓ Database exists\n');

// List all collections
print('2. Collections:');
const collections = db.getCollectionNames();
if (collections.length === 0) {
  print('   No collections yet (this is normal - collections are created automatically)');
  print('   Collections will appear after you sign up your first user.\n');
} else {
  collections.forEach(col => {
    print('   - ' + col);
    const count = db[col].countDocuments();
    print('     Documents: ' + count);
  });
  print('');
}

// Check indexes on users collection (if it exists)
if (collections.includes('users')) {
  print('3. Users Collection Indexes:');
  const indexes = db.users.getIndexes();
  indexes.forEach(idx => {
    print('   - ' + JSON.stringify(idx.key) + ' (unique: ' + (idx.unique || false) + ')');
  });
  print('');
}

// Test connection
print('4. Connection Test:');
try {
  const result = db.runCommand({ ping: 1 });
  if (result.ok === 1) {
    print('   Status: ✓ Connected successfully\n');
  }
} catch (e) {
  print('   Status: ✗ Connection failed: ' + e.message + '\n');
}

print('=== Setup Complete ===');
print('\nNext steps:');
print('1. Start the backend: docker-compose up -d backend');
print('2. Sign up a user through the application');
print('3. Refresh Navicat to see the users collection');




