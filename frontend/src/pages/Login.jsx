import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@context/AuthContext.jsx';
import { api } from '@services/apiService.js';
export default function Login() {
  const { login } = useAuth(); const navigate = useNavigate();
  const [email, setEmail] = useState('admin@db.com'); const [password, setPassword] = useState('admin123'); const [error, setError] = useState(null);
  async function submit(event) { event.preventDefault(); setError(null); try { const { token, role } = await api.login(email, password); login(token, role); navigate('/'); } catch (failure) { setError(failure.message || 'Login failed'); } }
  return <form onSubmit={submit} className="login-form"><h2>Sign in</h2><label>Email<input value={email} onChange={(event) => setEmail(event.target.value)} type="email" required /></label><label>Password<input value={password} onChange={(event) => setPassword(event.target.value)} type="password" required /></label>{error && <div role="alert" className="form-error">{error}</div>}<button type="submit">Sign in</button></form>;
}
