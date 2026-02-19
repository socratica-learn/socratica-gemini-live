# Update Your .env File

Here are the values to add to your `.env` file:

## MongoDB Connection String
```
SPRING_DATA_MONGODB_URI=mongodb+srv://socraticalearn_db_user:zF9JdKAA3zKF1DKp@cluster0.3h3exfm.mongodb.net/socratica_dev
```

## JWT Secret (Generated)
```
JWT_SECRET=vs0IWYNf8RwWJLoeTlCbNBLjY/E1N7Gc0HvuMWY803E=
```

## Your Complete .env File Should Look Like:

```env
SPRING_DATA_MONGODB_URI=mongodb+srv://socraticalearn_db_user:zF9JdKAA3zKF1DKp@cluster0.3h3exfm.mongodb.net/socratica_dev
MONGODB_DATABASE=socratica_dev
JWT_SECRET=vs0IWYNf8RwWJLoeTlCbNBLjY/E1N7Gc0HvuMWY803E=
OPENAI_API_KEY=
GEMINI_API_KEY=
GEMINI_MODEL=gemini-1.5-flash
GOOGLE_CLIENT_ID=your-google-client-id-here
GOOGLE_CLIENT_SECRET=your-google-client-secret-here
GOOGLE_REDIRECT_URI=http://localhost:8080/api/auth/oauth/google/callback
MICROSOFT_CLIENT_ID=
MICROSOFT_CLIENT_SECRET=
MICROSOFT_REDIRECT_URI=http://localhost:8080/api/auth/oauth/microsoft/callback
FRONTEND_URL=http://localhost:5173
```

## Steps:

1. Open your `.env` file at: `e:\TWENTE\Projects\socratica-1\.env`

2. Update these two lines:
   - Change `SPRING_DATA_MONGODB_URI=` to: `SPRING_DATA_MONGODB_URI=mongodb+srv://socraticalearn_db_user:zF9JdKAA3zKF1DKp@cluster0.3h3exfm.mongodb.net/socratica_dev`
   - Change `JWT_SECRET=` to: `JWT_SECRET=vs0IWYNf8RwWJLoeTlCbNBLjY/E1N7Gc0HvuMWY803E=`

3. Make sure your Google credentials are filled in (since Google OAuth works)

4. Save the file

5. Restart Docker containers:
   ```powershell
   docker compose down
   docker compose up
   ```

## What's Now Configured:

✅ MongoDB - Your data will be saved!
✅ JWT Secret - Secure token generation
✅ Google OAuth - Already working
⏳ OpenAI API Key - Only needed if you use AI features
⏳ Microsoft OAuth - Can add later when you have Azure access
