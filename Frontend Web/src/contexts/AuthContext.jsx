import React, { createContext, useContext, useState, useCallback } from 'react'
import { login as apiLogin } from '../api/client'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const token = localStorage.getItem('token')
    const email = localStorage.getItem('email')
    const role  = localStorage.getItem('role')
    return token ? { token, email, role } : null
  })

  const login = useCallback(async (email, password) => {
    const data = await apiLogin(email, password)
    localStorage.setItem('token', data.token)
    localStorage.setItem('email', data.email)
    localStorage.setItem('role',  data.role)
    setUser({ token: data.token, email: data.email, role: data.role })
    return data.role
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem('token')
    localStorage.removeItem('email')
    localStorage.removeItem('role')
    setUser(null)
  }, [])

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)
