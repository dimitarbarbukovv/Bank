import { useEffect, useRef, useState } from 'react'
import './App.css'

type ClientType = 'INDIVIDUAL' | 'COMPANY'
type CreditType = 'CONSUMER' | 'MORTGAGE'
type Menu = 'clients' | 'profile' | 'new-client' | 'employees' | 'my-account'
type ProfileTab = 'accounts' | 'credits'

interface Client {
  id?: number
  type: ClientType
  firstName?: string
  lastName?: string
  egn?: string
  companyName?: string
  eik?: string
  representativeName?: string
  createdByUsername?: string
  createdByDisplayName?: string
}

interface BankAccount {
  id?: number
  iban: string
  balance: number
  currency?: 'EUR' | 'USD' | 'BGN'
  status?: 'ACTIVE' | 'CLOSED'
  clientId: number
  createdByUsername?: string
  createdByDisplayName?: string
}

interface CreditSummary {
  id: number
  type: CreditType
  principal: number
  termMonths: number
  annualInterestRate: number
  createdAt: string
  status: 'NEW' | 'IN_PROGRESS' | 'PAID'
  createdByUsername?: string
  createdByDisplayName?: string
}

interface Employee {
  id: number
  username: string
  displayName?: string
  role: 'ADMIN' | 'EMPLOYEE'
  active: boolean
}

interface Installment {
  id: number
  monthNumber: number
  paymentAmount: number
  principalPart: number
  interestPart: number
  remainingPrincipal: number
  paid: boolean
}

const API = 'http://localhost:8080/api'

function App() {
  const [token, setToken] = useState<string | null>(null)
  const [role, setRole] = useState<string | null>(null)
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [toast, setToast] = useState<string | null>(null)

  const [menu, setMenu] = useState<Menu>('clients')
  const [profileTab, setProfileTab] = useState<ProfileTab>('accounts')

  const [filters, setFilters] = useState({ firstName: '', lastName: '', egn: '' })
  const [clients, setClients] = useState<Client[]>([])
  const [selectedClient, setSelectedClient] = useState<Client | null>(null)

  const [newClient, setNewClient] = useState<Client>({ type: 'INDIVIDUAL' })

  const [accounts, setAccounts] = useState<BankAccount[]>([])
  const [newBalance, setNewBalance] = useState(0)
  const [newCurrency, setNewCurrency] = useState<'EUR' | 'USD'>('EUR')
  const [depositAmounts, setDepositAmounts] = useState<Record<number, number>>({})

  const [credits, setCredits] = useState<CreditSummary[]>([])
  const [openedCreditId, setOpenedCreditId] = useState<number | null>(null)
  const [schedule, setSchedule] = useState<Installment[]>([])
  const [suggestedMax, setSuggestedMax] = useState<number | null>(null)
  const [creditForm, setCreditForm] = useState({
    type: 'CONSUMER' as CreditType,
    netIncome: 0,
    termMonths: 12,
    principal: 0,
    disbursementAccountId: 0,
    propertyValue: 0,
    downPayment: 0,
  })

  const [myProfile, setMyProfile] = useState<Employee | null>(null)
  const [profileDisplayName, setProfileDisplayName] = useState('')
  const [passwordForm, setPasswordForm] = useState({ current: '', next: '', next2: '' })

  const creditsListRef = useRef<HTMLDivElement>(null)

  const [employees, setEmployees] = useState<Employee[]>([])
  const [newEmployee, setNewEmployee] = useState({
    username: '',
    password: '',
    role: 'EMPLOYEE' as 'ADMIN' | 'EMPLOYEE',
    displayName: '',
  })

  const mortgageLoanAmount = Math.max(0, creditForm.propertyValue - creditForm.downPayment)
  const mortgageDownPaymentPercent = creditForm.propertyValue > 0
    ? (creditForm.downPayment / creditForm.propertyValue) * 100
    : 0

  const withAuth = (extra: Record<string, string> = {}) =>
    token ? { ...extra, Authorization: `Bearer ${token}` } : extra

  const showToast = (msg: string) => {
    setToast(msg)
    setTimeout(() => setToast(null), 2500)
  }

  const logout = () => {
    setToken(null)
    setRole(null)
    setPassword('')
    setError(null)
    setToast(null)
    setMenu('clients')
    setProfileTab('accounts')
    setSelectedClient(null)
    setClients([])
    setAccounts([])
    setCredits([])
    setSchedule([])
    setOpenedCreditId(null)
    setMyProfile(null)
  }

  const parseError = async (res: Response) => {
    const body = await res.json().catch(() => ({}))
    const ve = body?.validationErrors
    if (ve && typeof ve === 'object') {
      const parts = Object.values(ve as Record<string, string>).filter(Boolean)
      if (parts.length > 0) return parts.join(' ')
    }
    if (body?.error) return String(body.error)
    if (typeof body === 'object' && body !== null) {
      return Object.entries(body)
        .filter(([k]) => !['timestamp', 'status', 'path', 'validationErrors'].includes(k))
        .map(([k, v]) => `${k}: ${String(v)}`)
        .join(' | ')
    }
    return 'Възникна грешка'
  }

  const loadMe = async (authToken?: string) => {
    const headers = authToken ? { Authorization: `Bearer ${authToken}` } : withAuth()
    const res = await fetch(`${API}/auth/me`, { headers })
    if (res.ok) {
      const me = (await res.json()) as Employee
      setMyProfile(me)
      setProfileDisplayName(me.displayName ?? '')
    }
  }

  const login = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)
    const res = await fetch(`${API}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password }),
    })
    if (!res.ok) {
      setError('Грешен потребител или парола')
      return
    }
    const body = await res.json()
    setToken(body.token)
    setRole(body.role)
    loadAllClients(body.token)
  }

  useEffect(() => {
    if (token) loadMe()
  }, [token])

  useEffect(() => {
    if (!error) return
    const t = window.setTimeout(() => setError(null), 8000)
    return () => window.clearTimeout(t)
  }, [error])

  const loadEmployees = async () => {
    const res = await fetch(`${API}/employees`, { headers: withAuth() })
    if (!res.ok) {
      setError(await parseError(res))
      return
    }
    setEmployees(await res.json())
  }

  const createEmployee = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)
    const res = await fetch(`${API}/employees`, {
      method: 'POST',
      headers: withAuth({ 'Content-Type': 'application/json' }),
      body: JSON.stringify({
        username: newEmployee.username,
        password: newEmployee.password,
        role: newEmployee.role,
        displayName: newEmployee.displayName || undefined,
      }),
    })
    if (!res.ok) {
      setError(await parseError(res))
      return
    }
    showToast('Служителят е създаден')
    setNewEmployee({ username: '', password: '', role: 'EMPLOYEE', displayName: '' })
    loadEmployees()
  }

  const saveMyProfile = async (e: React.FormEvent) => {
    e.preventDefault()
    const res = await fetch(`${API}/auth/me`, {
      method: 'PATCH',
      headers: withAuth({ 'Content-Type': 'application/json' }),
      body: JSON.stringify({ displayName: profileDisplayName }),
    })
    if (!res.ok) {
      setError(await parseError(res))
      return
    }
    const me = (await res.json()) as Employee
    setMyProfile(me)
    showToast('Профилът е обновен')
  }

  const changePassword = async (e: React.FormEvent) => {
    e.preventDefault()
    if (passwordForm.next !== passwordForm.next2) {
      setError('Новите пароли не съвпадат')
      return
    }
    const res = await fetch(`${API}/auth/change-password`, {
      method: 'POST',
      headers: withAuth({ 'Content-Type': 'application/json' }),
      body: JSON.stringify({ currentPassword: passwordForm.current, newPassword: passwordForm.next }),
    })
    if (!res.ok) {
      setError(await parseError(res))
      return
    }
    showToast('Паролата е сменена')
    setPasswordForm({ current: '', next: '', next2: '' })
  }

  const searchClients = async () => {
    setError(null)
    const q = [filters.firstName, filters.lastName, filters.egn].filter(Boolean).join(' ')
    const res = await fetch(
      q.trim() ? `${API}/clients?q=${encodeURIComponent(q)}` : `${API}/clients`,
      { headers: withAuth() },
    )
    if (!res.ok) {
      setError(await parseError(res))
      return
    }
    const data = (await res.json()) as Client[]
    const refined = data.filter((c) =>
      (!filters.firstName || (c.firstName ?? '').toLowerCase().includes(filters.firstName.toLowerCase())) &&
      (!filters.lastName || (c.lastName ?? '').toLowerCase().includes(filters.lastName.toLowerCase())) &&
      (!filters.egn || (c.egn ?? '').includes(filters.egn)),
    )
    setClients(refined)
  }

  const loadAllClients = async (authToken?: string) => {
    const headers = authToken ? { Authorization: `Bearer ${authToken}` } : withAuth()
    const res = await fetch(`${API}/clients`, { headers })
    if (!res.ok) {
      setError(await parseError(res))
      return
    }
    setClients(await res.json())
  }

  const openClient = async (client: Client) => {
    setSelectedClient(client)
    setMenu('profile')
    setProfileTab('accounts')
    setOpenedCreditId(null)
    setSchedule([])
    await Promise.all([loadAccounts(client.id!), loadCredits(client.id!)])
  }

  const loadAccounts = async (clientId: number) => {
    const res = await fetch(`${API}/accounts/by-client/${clientId}`, { headers: withAuth() })
    if (res.ok) setAccounts(await res.json())
  }

  const loadCredits = async (clientId: number) => {
    const res = await fetch(`${API}/credits/by-client/${clientId}`, { headers: withAuth() })
    if (!res.ok) {
      setCredits([])
      setError(await parseError(res))
      return
    }
    setCredits(await res.json())
  }

  useEffect(() => {
    if (menu === 'profile' && profileTab === 'credits' && selectedClient?.id) {
      loadCredits(selectedClient.id)
    }
  }, [menu, profileTab, selectedClient?.id])

  useEffect(() => {
    if (menu === 'employees' && role === 'ROLE_ADMIN') {
      loadEmployees()
    }
  }, [menu, role])

  useEffect(() => {
    if (creditForm.type === 'MORTGAGE') {
      setCreditForm((prev) => ({ ...prev, principal: Math.max(0, prev.propertyValue - prev.downPayment) }))
    }
  }, [creditForm.type, creditForm.propertyValue, creditForm.downPayment])

  const createClient = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)
    if (newClient.type === 'INDIVIDUAL') {
      const fn = (newClient.firstName ?? '').trim()
      const ln = (newClient.lastName ?? '').trim()
      const egn = (newClient.egn ?? '').trim()
      if (fn.length <= 3) {
        setError('Името трябва да е поне 4 символа (повече от 3).')
        return
      }
      if (ln.length <= 3) {
        setError('Фамилията трябва да е поне 4 символа (повече от 3).')
        return
      }
      if (!/^\d{10}$/.test(egn)) {
        setError('ЕГН трябва да съдържа точно 10 цифри.')
        return
      }
    } else {
      const eik = (newClient.eik ?? '').trim()
      if (!/^\d{10}$/.test(eik)) {
        setError('ЕИК трябва да съдържа точно 10 цифри.')
        return
      }
    }
    const res = await fetch(`${API}/clients`, {
      method: 'POST',
      headers: withAuth({ 'Content-Type': 'application/json' }),
      body: JSON.stringify(newClient),
    })
    if (!res.ok) {
      setError(await parseError(res))
      return
    }
    showToast('Клиентът е създаден')
    setNewClient({ type: 'INDIVIDUAL' })
    setMenu('clients')
  }

  const addAccount = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!selectedClient?.id) return
    const res = await fetch(`${API}/accounts`, {
      method: 'POST',
      headers: withAuth({ 'Content-Type': 'application/json' }),
      body: JSON.stringify({ clientId: selectedClient.id, balance: newBalance, currency: newCurrency }),
    })
    if (!res.ok) {
      setError(await parseError(res))
      return
    }
    showToast('Сметката е създадена')
    setNewBalance(0)
    setNewCurrency('EUR')
    loadAccounts(selectedClient.id)
  }

  const closeAccount = async (accountId: number) => {
    const res = await fetch(`${API}/accounts/${accountId}/close`, {
      method: 'POST',
      headers: withAuth(),
    })
    if (!res.ok) {
      setError(await parseError(res))
      return
    }
    showToast('Сметката е закрита')
    if (selectedClient?.id) loadAccounts(selectedClient.id)
  }

  const depositToAccount = async (accountId: number) => {
    const amount = depositAmounts[accountId] ?? 0
    if (amount <= 0) {
      setError('Въведи валидна сума за внасяне')
      return
    }
    const res = await fetch(`${API}/accounts/${accountId}/deposit`, {
      method: 'POST',
      headers: withAuth({ 'Content-Type': 'application/json' }),
      body: JSON.stringify({ amount }),
    })
    if (!res.ok) {
      setError(await parseError(res))
      return
    }
    showToast('Сумата е внесена успешно')
    setDepositAmounts((prev) => ({ ...prev, [accountId]: 0 }))
    if (selectedClient?.id) loadAccounts(selectedClient.id)
  }

  const calculateSuggestion = async () => {
    if (!creditForm.netIncome || !creditForm.termMonths) return
    const query = new URLSearchParams({
      type: creditForm.type,
      netIncome: String(creditForm.netIncome),
      termMonths: String(creditForm.termMonths),
    })
    if (creditForm.type === 'MORTGAGE') {
      query.set('propertyValue', String(creditForm.propertyValue))
      query.set('downPayment', String(creditForm.downPayment))
    }
    const res = await fetch(
      `${API}/credits/suggestion?${query.toString()}`,
      { headers: withAuth() },
    )
    if (!res.ok) return
    const body = await res.json()
    setSuggestedMax(Number(body.maxPrincipal))
  }

  const createCredit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!selectedClient?.id) return
    if (creditForm.netIncome <= 0 || creditForm.termMonths <= 0 || creditForm.principal <= 0) {
      setError('Попълни валидни стойности за доход, срок и сума')
      return
    }
    if (creditForm.type === 'MORTGAGE') {
      if (creditForm.propertyValue <= 0) {
        setError('Въведи стойност на имота за ипотечен кредит')
        return
      }
      if (creditForm.downPayment < 0) {
        setError('Самоучастието не може да е отрицателно')
        return
      }
      if (mortgageDownPaymentPercent < 20) {
        setError('Самоучастието трябва да е поне 20%')
        return
      }
      if (Math.abs(creditForm.principal - mortgageLoanAmount) > 0.01) {
        setError('Ипотечната сума трябва да е равна на стойност имот - самоучастие')
        return
      }
    }
    if (creditForm.type === 'CONSUMER' && !creditForm.disbursementAccountId) {
      setError('Избери сметка за превод при потребителски кредит')
      return
    }
    const res = await fetch(`${API}/credits`, {
      method: 'POST',
      headers: withAuth({ 'Content-Type': 'application/json' }),
      body: JSON.stringify({
        clientId: selectedClient.id,
        type: creditForm.type,
        principal: creditForm.principal,
        termMonths: creditForm.termMonths,
        netIncome: creditForm.netIncome,
        disbursementAccountId:
          creditForm.type === 'CONSUMER' && creditForm.disbursementAccountId > 0
            ? creditForm.disbursementAccountId
            : undefined,
        propertyValue: creditForm.type === 'MORTGAGE' ? creditForm.propertyValue : undefined,
        downPayment: creditForm.type === 'MORTGAGE' ? creditForm.downPayment : undefined,
      }),
    })
    if (!res.ok) {
      setError(await parseError(res))
      return
    }
    const body = await res.json()
    const id = Number(body.id)
    setOpenedCreditId(id)
    showToast(`Кредит #${id} е отпуснат`)
    openCredit(id)
    await loadCredits(selectedClient.id)
    await loadAccounts(selectedClient.id)
    window.requestAnimationFrame(() => {
      window.requestAnimationFrame(() => {
        creditsListRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' })
      })
    })
  }

  const openCredit = async (creditId: number) => {
    setOpenedCreditId(creditId)
    const res = await fetch(`${API}/credits/${creditId}/schedule`, { headers: withAuth() })
    if (res.ok) setSchedule(await res.json())
  }

  const markPaid = async (installmentId: number) => {
    if (!openedCreditId) return
    const res = await fetch(`${API}/credits/installments/${installmentId}/pay`, {
      method: 'POST',
      headers: withAuth(),
    })
    if (!res.ok) {
      setError(await parseError(res))
      return
    }
    openCredit(openedCreditId)
  }

  const refreshCredits = async () => {
    if (!selectedClient?.id) return
    await loadCredits(selectedClient.id)
    if (openedCreditId) {
      await openCredit(openedCreditId)
    }
    showToast('Данните за кредитите са обновени')
  }

  if (!token) {
    return (
      <div className="auth-shell">
        <div className="auth-card">
          <div className="auth-brand" aria-label="Bank Control Center">
            <div className="auth-heading">Bank Control Center</div>
            <div className="auth-subtitle">Сигурен достъп за служители</div>
          </div>
          {error && <div className="error">{error}</div>}
          <form className="auth-form" onSubmit={login}>
            <label className="auth-field">
              <span>Потребител</span>
              <input
                className="auth-input"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                autoComplete="username"
              />
            </label>
            <label className="auth-field">
              <span>Парола</span>
              <input
                className="auth-input"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                autoComplete="current-password"
              />
            </label>
            <button className="auth-button" type="submit">Вход</button>
          </form>
        </div>
      </div>
    )
  }

  return (
    <div className="app">
      <header className="topbar">
        <div className="logo">🏦 Bank Portal</div>
        <nav className="menu">
          <button className={menu === 'clients' ? 'active' : ''} onClick={() => setMenu('clients')}>Клиенти</button>
          <button className={menu === 'profile' ? 'active' : ''} onClick={() => setMenu('profile')}>Профил клиент</button>
          {role === 'ROLE_ADMIN' && (
            <button className={menu === 'employees' ? 'active' : ''} onClick={() => setMenu('employees')}>Служители</button>
          )}
          <button className={menu === 'my-account' ? 'active' : ''} onClick={() => setMenu('my-account')}>Моят профил</button>
        </nav>
        <div className="userbox">
          {myProfile?.displayName || username} • {role === 'ROLE_ADMIN' ? 'Админ' : 'Служител'}
          <button type="button" className="logout-btn" onClick={logout}>Изход</button>
        </div>
      </header>

      {error && <div className="error">{error}</div>}
      {toast && <div className="toast">{toast}</div>}

      {menu === 'clients' && (
        <section className="page">
          <h2>Клиенти</h2>
          <div className="clients-actions">
            <button className="btn-green" onClick={() => setMenu('new-client')}>+ Нов клиент</button>
          </div>
          <div className="filters">
            <input placeholder="Първо име" value={filters.firstName} onChange={(e) => setFilters({ ...filters, firstName: e.target.value })} />
            <input placeholder="Фамилия" value={filters.lastName} onChange={(e) => setFilters({ ...filters, lastName: e.target.value })} />
            <input placeholder="ЕГН" value={filters.egn} onChange={(e) => setFilters({ ...filters, egn: e.target.value })} />
            <button onClick={searchClients}>Търси</button>
            <button
              type="button"
              onClick={() => {
                setFilters({ firstName: '', lastName: '', egn: '' })
                loadAllClients()
              }}
            >
              Изчисти
            </button>
          </div>
          <table className="table clients-table">
            <thead><tr><th>Тип</th><th>Име</th><th>Фамилия/Фирма</th><th>ЕГН/ЕИК</th><th>Регистрирал</th></tr></thead>
            <tbody>
              {clients.map((c) => (
                <tr key={c.id} className="clickable-row" onClick={() => openClient(c)}>
                  <td>{c.type === 'INDIVIDUAL' ? 'ФЛ' : 'ЮЛ'}</td>
                  <td>{c.type === 'INDIVIDUAL' ? (c.firstName ?? '—') : (c.representativeName ?? '—')}</td>
                  <td>{c.type === 'INDIVIDUAL' ? (c.lastName ?? '—') : (c.companyName ?? '—')}</td>
                  <td>{c.egn ?? c.eik}</td>
                  <td>{c.createdByDisplayName ?? c.createdByUsername ?? '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </section>
      )}

      {menu === 'new-client' && (
        <section className="page">
          <h2>Добавяне на клиент</h2>
          <form className="form wide-form new-client-form" onSubmit={createClient}>
            <label className="new-client-row-full">Тип
              <select value={newClient.type} onChange={(e) => setNewClient({ ...newClient, type: e.target.value as ClientType })}>
                <option value="INDIVIDUAL">Физическо лице</option>
                <option value="COMPANY">Юридическо лице</option>
              </select>
            </label>
            {newClient.type === 'INDIVIDUAL' ? (
              <>
                <label>Име<input required value={newClient.firstName ?? ''} onChange={(e) => setNewClient({ ...newClient, firstName: e.target.value })} /></label>
                <label>Фамилия<input required value={newClient.lastName ?? ''} onChange={(e) => setNewClient({ ...newClient, lastName: e.target.value })} /></label>
                <label className="new-client-row-full">ЕГН (10 цифри)<input required inputMode="numeric" maxLength={10} autoComplete="off" value={newClient.egn ?? ''} onChange={(e) => setNewClient({ ...newClient, egn: e.target.value.replace(/\D/g, '').slice(0, 10) })} /></label>
              </>
            ) : (
              <>
                <label>Фирма<input required value={newClient.companyName ?? ''} onChange={(e) => setNewClient({ ...newClient, companyName: e.target.value })} /></label>
                <label>ЕИК (10 цифри)<input required inputMode="numeric" maxLength={10} autoComplete="off" value={newClient.eik ?? ''} onChange={(e) => setNewClient({ ...newClient, eik: e.target.value.replace(/\D/g, '').slice(0, 10) })} /></label>
                <label className="new-client-row-full">Представител<input required value={newClient.representativeName ?? ''} onChange={(e) => setNewClient({ ...newClient, representativeName: e.target.value })} /></label>
              </>
            )}
            <button type="submit">Запази клиент</button>
          </form>
        </section>
      )}

      {menu === 'employees' && role === 'ROLE_ADMIN' && (
        <section className="page">
          <h2>Служители</h2>
          <p className="muted">Само администратор може да създава и управлява акаунти на служители.</p>
          <form className="form wide-form employee-form" onSubmit={createEmployee}>
            <label>Потребителско име<input required value={newEmployee.username} onChange={(e) => setNewEmployee({ ...newEmployee, username: e.target.value })} /></label>
            <label>Парола<input required type="password" value={newEmployee.password} onChange={(e) => setNewEmployee({ ...newEmployee, password: e.target.value })} /></label>
            <label>Име за показване<input value={newEmployee.displayName} onChange={(e) => setNewEmployee({ ...newEmployee, displayName: e.target.value })} placeholder="По избор" /></label>
            <label>Роля
              <select value={newEmployee.role} onChange={(e) => setNewEmployee({ ...newEmployee, role: e.target.value as 'ADMIN' | 'EMPLOYEE' })}>
                <option value="EMPLOYEE">Служител</option>
                <option value="ADMIN">Администратор</option>
              </select>
            </label>
            <button type="submit">Създай служител</button>
          </form>
          <table className="table">
            <thead><tr><th>Потребител</th><th>Име</th><th>Роля</th><th>Активен</th></tr></thead>
            <tbody>
              {employees.map((em) => (
                <tr key={em.id}>
                  <td>{em.username}</td>
                  <td>{em.displayName ?? '—'}</td>
                  <td>{em.role === 'ADMIN' ? 'Админ' : 'Служител'}</td>
                  <td>{em.active ? 'Да' : 'Не'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </section>
      )}

      {menu === 'my-account' && (
        <section className="page">
          <div className="my-account-stack">
            <h2>Моят профил</h2>
            <form className="form wide-form my-account-form" onSubmit={saveMyProfile}>
              <label>Потребителско име<input readOnly value={myProfile?.username ?? username} /></label>
              <label>Име за показване<input value={profileDisplayName} onChange={(e) => setProfileDisplayName(e.target.value)} placeholder="Как да те виждат колегите" /></label>
              <button type="submit">Запази профил</button>
            </form>
            <h3>Смяна на парола</h3>
            <form className="form wide-form my-account-form password-change-form" onSubmit={changePassword}>
              <label>Текуща парола<input type="password" required value={passwordForm.current} onChange={(e) => setPasswordForm({ ...passwordForm, current: e.target.value })} /></label>
              <label>Нова парола<input type="password" required value={passwordForm.next} onChange={(e) => setPasswordForm({ ...passwordForm, next: e.target.value })} /></label>
              <label>Повтори новата парола<input type="password" required value={passwordForm.next2} onChange={(e) => setPasswordForm({ ...passwordForm, next2: e.target.value })} /></label>
              <button type="submit">Смени парола</button>
            </form>
          </div>
        </section>
      )}

      {menu === 'profile' && (
        <section className="page">
          {!selectedClient && <p>Избери клиент от екрана „Клиенти“.</p>}
          {selectedClient && (
            <>
              <h2>Профил на клиент</h2>
              <div className="profile-card">
                <div><strong>Тип:</strong> {selectedClient.type === 'INDIVIDUAL' ? 'Физическо лице' : 'Юридическо лице'}</div>
                {selectedClient.type === 'INDIVIDUAL' ? (
                  <>
                    <div><strong>Име:</strong> {selectedClient.firstName ?? '—'}</div>
                    <div><strong>Фамилия:</strong> {selectedClient.lastName ?? '—'}</div>
                    <div><strong>ЕГН:</strong> {selectedClient.egn ?? '—'}</div>
                  </>
                ) : (
                  <>
                    <div><strong>Фирма:</strong> {selectedClient.companyName ?? '—'}</div>
                    <div><strong>Представител:</strong> {selectedClient.representativeName ?? '—'}</div>
                    <div><strong>ЕИК:</strong> {selectedClient.eik ?? '—'}</div>
                  </>
                )}
                {(selectedClient.createdByDisplayName || selectedClient.createdByUsername) && (
                  <div><strong>Регистрирал клиента:</strong> {selectedClient.createdByDisplayName ?? selectedClient.createdByUsername}</div>
                )}
              </div>

              <div className="tabs">
                <button className={profileTab === 'accounts' ? 'tab active' : 'tab'} onClick={() => setProfileTab('accounts')}>Сметки</button>
                <button className={profileTab === 'credits' ? 'tab active' : 'tab'} onClick={() => setProfileTab('credits')}>Кредитиране</button>
              </div>

              {profileTab === 'accounts' && (
                <>
                  <form className="filters" onSubmit={addAccount}>
                    <input type="number" min={0} value={newBalance} onChange={(e) => setNewBalance(Number(e.target.value))} placeholder="Начална наличност" />
                    <select value={newCurrency} onChange={(e) => setNewCurrency(e.target.value as 'EUR' | 'USD')}>
                      <option value="EUR">EUR</option>
                      <option value="USD">USD</option>
                    </select>
                    <button type="submit">Добави сметка</button>
                  </form>
                  <table className="table">
                    <thead><tr><th>IBAN</th><th>Наличност</th><th>Валута</th><th>Статус</th><th>Открил</th><th>Действие</th></tr></thead>
                    <tbody>
                      {accounts.map((a) => (
                        <tr key={a.id}>
                          <td>{a.iban}</td>
                          <td>{a.balance.toFixed(2)}</td>
                          <td>{a.currency ?? 'EUR'}</td>
                          <td>{a.status}</td>
                          <td>{a.createdByDisplayName ?? a.createdByUsername ?? '—'}</td>
                          <td>
                            <div className="account-actions">
                              <input
                                type="number"
                                min={0}
                                step="0.01"
                                placeholder="Сума за внасяне"
                                value={depositAmounts[a.id!] ?? 0}
                                onChange={(e) =>
                                  setDepositAmounts((prev) => ({ ...prev, [a.id!]: Number(e.target.value) }))
                                }
                                disabled={a.status === 'CLOSED'}
                              />
                              <button disabled={a.status === 'CLOSED'} onClick={() => depositToAccount(a.id!)}>
                                Внеси
                              </button>
                            </div>
                            <button disabled={a.balance !== 0 || a.status === 'CLOSED'} onClick={() => closeAccount(a.id!)}>
                              Закрий
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </>
              )}

              {profileTab === 'credits' && (
                <>
                  <div className="calculator-card">
                    <div className="calculator-head">
                      <h3>Кредитен калкулатор</h3>
                      <p>Професионална оценка по доход, срок, лихва и обезпечение.</p>
                    </div>

                    <form className="credit-calculator" onSubmit={createCredit}>
                      <label>
                        Вид кредит
                        <select
                          value={creditForm.type}
                          onChange={(e) => setCreditForm({ ...creditForm, type: e.target.value as CreditType })}
                        >
                          <option value="CONSUMER">Потребителски</option>
                          <option value="MORTGAGE">Ипотечен</option>
                        </select>
                      </label>

                      <label>
                        Нетен месечен доход (евро)
                        <input
                          type="number"
                          min={0}
                          placeholder="напр. 2800"
                          value={creditForm.netIncome}
                          onChange={(e) => setCreditForm({ ...creditForm, netIncome: Number(e.target.value) })}
                        />
                        <small>Максималната месечна вноска е до 30% от дохода.</small>
                      </label>

                      <label>
                        Срок (месеци)
                        <input
                          type="number"
                          min={1}
                          placeholder="напр. 120"
                          value={creditForm.termMonths}
                          onChange={(e) => setCreditForm({ ...creditForm, termMonths: Number(e.target.value) })}
                        />
                        <small>По-дълъг срок намалява месечната вноска.</small>
                      </label>

                      {creditForm.type === 'MORTGAGE' && (
                        <label>
                          Стойност на имота (евро)
                          <input
                            type="number"
                            min={0}
                            placeholder="напр. 180000"
                            value={creditForm.propertyValue}
                            onChange={(e) => setCreditForm({ ...creditForm, propertyValue: Number(e.target.value) })}
                          />
                        </label>
                      )}

                      {creditForm.type === 'MORTGAGE' && (
                        <label>
                          Самоучастие (евро)
                          <input
                            type="number"
                            min={0}
                            placeholder="напр. 40000"
                            value={creditForm.downPayment}
                            onChange={(e) => setCreditForm({ ...creditForm, downPayment: Number(e.target.value) })}
                          />
                          <small>Минимум 20% от стойността на имота.</small>
                        </label>
                      )}

                      <label>
                        Искана сума (евро)
                        <input
                          type="number"
                          min={100}
                          placeholder="напр. 12000"
                          value={creditForm.principal}
                          readOnly={creditForm.type === 'MORTGAGE'}
                          onChange={(e) => setCreditForm({ ...creditForm, principal: Number(e.target.value) })}
                        />
                        {creditForm.type === 'MORTGAGE' && (
                          <small>Изчислява се автоматично: имот - самоучастие.</small>
                        )}
                      </label>

                      {creditForm.type === 'CONSUMER' && (
                        <label>
                          Сметка за превод
                          <select
                            value={creditForm.disbursementAccountId}
                            onChange={(e) =>
                              setCreditForm({ ...creditForm, disbursementAccountId: Number(e.target.value) })
                            }
                          >
                            <option value={0}>Избери сметка</option>
                            {accounts.map((a) => (
                              <option key={a.id} value={a.id}>
                                {a.iban}
                              </option>
                            ))}
                          </select>
                          <small>При потребителски кредит сумата се превежда по тази сметка.</small>
                        </label>
                      )}

                      <div className="calc-actions">
                        <button type="button" onClick={calculateSuggestion}>
                          Изчисли максимум
                        </button>
                        <button type="submit">Отпусни кредит</button>
                      </div>
                    </form>

                    {creditForm.type === 'MORTGAGE' && (
                      <div className="mortgage-metrics">
                        <div>Самоучастие: <strong>{mortgageDownPaymentPercent.toFixed(2)}%</strong></div>
                        <div>Ипотечна сума: <strong>{mortgageLoanAmount.toFixed(2)} евро</strong></div>
                      </div>
                    )}

                    {suggestedMax !== null && <div className="hint">Препоръчителен максимум: {suggestedMax.toFixed(2)} евро</div>}
                    <details className="calculator-note-details">
                      <summary>Как се калкулира (покажи)</summary>
                      <div className="calculator-note-body">
                        Анюитетна вноска с динамична лихва според тип кредит и доход. За ипотечен кредит се взема предвид и
                        самоучастието (мин. 20%), а максималната сума е ограничена както от дохода, така и от стойността на
                        имота.
                      </div>
                    </details>
                  </div>

                  <div className="credits-list-anchor" ref={creditsListRef}>
                    <div className="credits-toolbar">
                      <h3 className="credits-list-title">Отпуснати кредити</h3>
                      <button type="button" onClick={refreshCredits}>Обнови</button>
                    </div>

                    <table className="table">
                    <thead><tr><th>ID</th><th>Тип</th><th>Сума</th><th>Лихва</th><th>Срок</th><th>Дата</th><th>Статус</th><th>Отпуснал</th><th></th></tr></thead>
                    <tbody>
                      {credits.map((c) => (
                        <tr key={c.id}>
                          <td>{c.id}</td>
                          <td>{c.type}</td>
                          <td>{c.principal.toFixed(2)}</td>
                          <td>{c.annualInterestRate}%</td>
                          <td>{c.termMonths}</td>
                          <td>{new Date(c.createdAt).toLocaleDateString('bg-BG')}</td>
                          <td>{c.status}</td>
                          <td>{c.createdByDisplayName ?? c.createdByUsername ?? '—'}</td>
                          <td><button onClick={() => openCredit(c.id)}>Детайли</button></td>
                        </tr>
                      ))}
                      {credits.length === 0 && (
                        <tr>
                          <td colSpan={9}>Няма отпуснати кредити за този клиент.</td>
                        </tr>
                      )}
                    </tbody>
                  </table>
                  </div>

                  {openedCreditId && (
                    <div className="schedule-box">
                      <h3>Погасителен план — кредит #{openedCreditId}</h3>
                      <table className="table">
                        <thead><tr><th>Месец</th><th>Вноска</th><th>Главница</th><th>Лихва</th><th>Остатък</th><th></th></tr></thead>
                        <tbody>
                          {schedule.map((s) => (
                            <tr key={s.id}>
                              <td>{s.monthNumber}</td>
                              <td>{s.paymentAmount.toFixed(2)}</td>
                              <td>{s.principalPart.toFixed(2)}</td>
                              <td>{s.interestPart.toFixed(2)}</td>
                              <td>{s.remainingPrincipal.toFixed(2)}</td>
                              <td>{!s.paid && <button onClick={() => markPaid(s.id)}>Платена</button>}</td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  )}
                </>
              )}
            </>
          )}
        </section>
      )}
    </div>
  )
}

export default App

