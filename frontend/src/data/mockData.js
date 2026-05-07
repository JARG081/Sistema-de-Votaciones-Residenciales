export const dashboardStats = [
  { label: 'Asistencia media', value: '87%', detail: 'Últimos 6 conjuntos' },
  { label: 'Residentes activos', value: '148', detail: 'Cuentas habilitadas' },
  { label: 'Encuestas abiertas', value: '04', detail: 'En curso y próximas' },
  { label: 'Respuesta promedio', value: '91%', detail: 'Participación total' }
];

export const participationChart = {
  labels: ['Participaron', 'Pendientes'],
  values: [87, 13]
};

export const featuredSurvey = {
  title: 'Encuesta del conjunto',
  question: '¿Aprueba la instalación de iluminación LED en las zonas comunes?',
  options: [
    { label: 'Sí, a favor', percentage: 54, votes: 134 },
    { label: 'No por ahora', percentage: 28, votes: 69 },
    { label: 'Necesito más información', percentage: 18, votes: 44 },
    { label: 'Me abstengo', percentage: 6, votes: 15 }
  ]
};

export const resultsDataset = {
  labels: ['Sí', 'No', 'Abstención', 'Pendiente'],
  values: [134, 69, 15, 44]
};

export const historyItems = [
  { title: 'Mantenimiento de ascensores', date: '25/04/2026', status: 'Próxima', votes: 'Pendiente' },
  { title: 'Reparación de cámaras', date: '16/04/2026', status: 'En curso', votes: '78%' },
  { title: 'Presupuesto 2026', date: '10/03/2026', status: 'Finalizada', votes: '92%' },
  { title: 'Reglamento interno', date: '23/02/2026', status: 'Finalizada', votes: '89%' }
];

export const surveyQuestion = {
  title: 'Votación en curso',
  question: '¿Está de acuerdo con habilitar la reserva de zonas comunes desde el portal?',
  options: [
    'Sí, lo apoyo',
    'No por el momento',
    'Depende de las condiciones',
    'Prefiero abstenerme'
  ]
};
