export async function postForm(path, data) {
  const body = new URLSearchParams();

  Object.entries(data).forEach(([key, value]) => {
    if (Array.isArray(value)) {
      value.forEach((entry) => body.append(key, entry));
      return;
    }

    if (value !== undefined && value !== null) {
      body.append(key, value);
    }
  });

  return fetch(path, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
    },
    body: body.toString()
  });
}

async function parseJsonResponse(response, fallbackMessage) {
  const contentType = response.headers.get('content-type') || '';
  let payload = null;
  try {
    payload = contentType.includes('application/json') ? await response.json() : null;
  } catch {
    payload = null;
  }

  if (payload && typeof payload === 'object') {
    return {
      success: response.ok && payload.success !== false,
      ...payload
    };
  }

  return {
    success: response.ok,
    message: response.ok ? fallbackMessage : fallbackMessage
  };
}

export async function submitLogin(email, password) {
  const response = await postForm('/api/logincheck', { email, password });
  return parseJsonResponse(response, 'No fue posible iniciar sesión.');
}

export async function submitRegistration(payload) {
  const response = await postForm('/api/registration', payload);
  return parseJsonResponse(response, 'No fue posible registrar la cuenta.');
}

export async function submitVoteCode(codigo) {
  const response = await postForm('/api/ingresar_codigo', { codigo });
  return parseJsonResponse(response, 'No fue posible validar el código.');
}

export async function submitVote(voterCode, selectedOption) {
  const response = await postForm('/api/vote/submit', { voterCode, selectedOption });
  return parseJsonResponse(response, 'No fue posible registrar el voto.');
}

export async function submitSurvey(payload) {
  const response = await postForm('/api/survey/save', payload);
  return parseJsonResponse(response, 'No fue posible guardar la encuesta.');
}

export async function submitAdminSurvey(payload) {
  const response = await postForm('/api/admin/survey/save', payload);
  return parseJsonResponse(response, 'No fue posible guardar la encuesta.');
}

export async function updateSurveyStatus(surveyId, status, actor) {
  const response = await postForm(`/api/admin/surveys/${surveyId}/status`, { status, actor });
  return parseJsonResponse(response, 'No fue posible actualizar el estado de la encuesta.');
}

export async function fetchResidentPadron(searchTerm = '') {
  try {
    const url = searchTerm.trim() ? `/api/admin/padron/search?q=${encodeURIComponent(searchTerm.trim())}` : '/api/admin/padron';
    const response = await fetch(url, {
      method: 'GET',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json'
      }
    });
    if (!response.ok) throw new Error('Failed to fetch resident padron');
    return await response.json();
  } catch (error) {
    console.error('Error fetching resident padron:', error);
    return [];
  }
}

export async function updateResidentHousing(userId, blockName, towerName, unitNumber) {
  const response = await postForm(`/api/admin/padron/${userId}/housing`, { blockName, towerName, unitNumber });
  return parseJsonResponse(response, 'No fue posible actualizar la vivienda del residente.');
}

export async function blockResident(userId, blocked) {
  const response = await postForm(`/api/admin/padron/${userId}/block`, { blocked });
  return parseJsonResponse(response, 'No fue posible actualizar el estado del residente.');
}

export async function fetchSurveyVotes(surveyId) {
  try {
    const response = await fetch(`/api/admin/surveys/${surveyId}/votes`, {
      method: 'GET',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json'
      }
    });
    if (!response.ok) throw new Error('Failed to fetch survey votes');
    return await response.json();
  } catch (error) {
    console.error('Error fetching survey votes:', error);
    return [];
  }
}

export async function fetchParticipationSummary(dimension = 'block') {
  try {
    const response = await fetch(`/api/admin/participation?dimension=${encodeURIComponent(dimension)}`, {
      method: 'GET',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json'
      }
    });
    if (!response.ok) throw new Error('Failed to fetch participation summary');
    return await response.json();
  } catch (error) {
    console.error('Error fetching participation summary:', error);
    return [];
  }
}

export async function exportSurveyResults(surveyId) {
  const response = await fetch(`/api/admin/surveys/${surveyId}/export`, {
    method: 'GET',
    credentials: 'include'
  });

  if (!response.ok) {
    return { success: false, message: 'No fue posible exportar los resultados.' };
  }

  const csv = await response.text();
  return { success: true, csv };
}

export async function submitUserSave(payload) {
  const response = await postForm('/api/user/save', payload);
  return parseJsonResponse(response, 'No fue posible crear el usuario.');
}

export async function submitLogout() {
  const response = await postForm('/api/logout', {});
  return parseJsonResponse(response, 'No fue posible cerrar sesión.');
}

// GET endpoints for fetching data from backend
export async function fetchDashboardStats() {
  try {
    const response = await fetch('/api/data/dashboard-stats', {
      method: 'GET',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json'
      }
    });
    if (!response.ok) throw new Error('Failed to fetch dashboard stats');
    return await response.json();
  } catch (error) {
    console.error('Error fetching dashboard stats:', error);
    return [];
  }
}

export async function fetchParticipationChart() {
  try {
    const response = await fetch('/api/data/participation-chart', {
      method: 'GET',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json'
      }
    });
    if (!response.ok) throw new Error('Failed to fetch participation chart');
    return await response.json();
  } catch (error) {
    console.error('Error fetching participation chart:', error);
    return { labels: [], values: [] };
  }
}

export async function fetchCurrentSurvey() {
  try {
    const response = await fetch('/api/data/current-survey', {
      method: 'GET',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json'
      }
    });
    if (!response.ok) throw new Error('Failed to fetch current survey');
    return await response.json();
  } catch (error) {
    console.error('Error fetching current survey:', error);
    return { title: '', question: '', options: [] };
  }
}

export async function fetchSurveyHistory(searchTerm = '', status = '') {
  try {
    if (searchTerm.trim()) {
      const response = await fetch(`/api/admin/surveys/search?q=${encodeURIComponent(searchTerm.trim())}`, {
        method: 'GET',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json'
        }
      });
      if (!response.ok) throw new Error('Failed to search survey history');
      return await response.json();
    }

    if (status.trim()) {
      const response = await fetch(`/api/admin/surveys/status/${encodeURIComponent(status.trim())}`, {
        method: 'GET',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json'
        }
      });
      if (!response.ok) throw new Error('Failed to filter survey history by status');
      return await response.json();
    }

    const response = await fetch('/api/data/survey-history', {
      method: 'GET',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json'
      }
    });
    if (!response.ok) throw new Error('Failed to fetch survey history');
    return await response.json();
  } catch (error) {
    console.error('Error fetching survey history:', error);
    return [];
  }
}

export async function fetchSurveysByStatus(status) {
  return fetchSurveyHistory('', status);
}

export async function fetchSurveyQuestion() {
  try {
    const response = await fetch('/api/data/survey-question', {
      method: 'GET',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json'
      }
    });
    if (!response.ok) throw new Error('Failed to fetch survey question');
    return await response.json();
  } catch (error) {
    console.error('Error fetching survey question:', error);
    return { title: '', question: '', options: [] };
  }
}

export async function fetchResultsDataset() {
  try {
    const response = await fetch('/api/data/results-dataset', {
      method: 'GET',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json'
      }
    });
    if (!response.ok) throw new Error('Failed to fetch results dataset');
    return await response.json();
  } catch (error) {
    console.error('Error fetching results dataset:', error);
    return { labels: [], values: [] };
  }
}
