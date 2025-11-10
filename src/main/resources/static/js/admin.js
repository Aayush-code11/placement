let studentsData = [];
let companiesData = [];
let applicationsData = [];

function logout() {
  sessionStorage.clear();
  window.location.href = '/';
}

async function checkAdminAuth() {
  const username = sessionStorage.getItem('username');
  const role = sessionStorage.getItem('role');
  
  if (username && role === 'ADMIN') {
    document.getElementById('authRequired').style.display = 'none';
    document.getElementById('dashboardContent').style.display = 'block';
    loadDashboardData();
  } else {
    document.getElementById('authRequired').style.display = 'block';
    document.getElementById('dashboardContent').style.display = 'none';
  }
}

async function loadDashboardData() {
  try {
    const [students, companies, applications] = await Promise.all([
      fetch('/api/students').then(r => r.json()),
      fetch('/api/companies').then(r => r.json()),
      fetch('/api/applications').then(r => r.json())
    ]);
    
    studentsData = students;
    companiesData = companies;
    applicationsData = applications;
    
    document.getElementById('totalStudents').textContent = students.length;
    document.getElementById('totalCompanies').textContent = companies.length;
    document.getElementById('totalApplications').textContent = applications.length;
    
    const placements = applications.filter(a => a.status && a.status.toLowerCase() === 'selected').length;
    document.getElementById('totalPlacements').textContent = placements;
    
    loadStudentsData();
  } catch (error) {
    console.error('Error loading dashboard:', error);
  }
}

function loadStudentsData() {
  if (studentsData.length === 0) {
    document.getElementById('studentsData').innerHTML = '<p class="no-data">No students registered yet</p>';
    return;
  }
  
  let html = '<table class="data-table"><thead><tr><th>ID</th><th>Name</th><th>Roll</th><th>Branch</th><th>CGPA</th><th>Email</th><th>Resume</th></tr></thead><tbody>';
  studentsData.forEach(s => {
    html += `<tr>
      <td>${s.id}</td>
      <td>${s.name || 'N/A'}</td>
      <td>${s.roll || 'N/A'}</td>
      <td>${s.branch || 'N/A'}</td>
      <td>${s.cgpa || 'N/A'}</td>
      <td>${s.email || 'N/A'}</td>
      <td>${s.resumeFileName ? '✅' : '❌'}</td>
    </tr>`;
  });
  html += '</tbody></table>';
  document.getElementById('studentsData').innerHTML = html;
}

function loadCompaniesData() {
  if (companiesData.length === 0) {
    document.getElementById('companiesData').innerHTML = '<p class="no-data">No companies added yet</p>';
    return;
  }
  
  let html = '<table class="data-table"><thead><tr><th>Company</th><th>Role</th><th>CTC</th><th>Location</th><th>Min CGPA</th><th>Branches</th><th>Openings</th></tr></thead><tbody>';
  companiesData.forEach(c => {
    html += `<tr>
      <td>${c.name}</td>
      <td>${c.jobRole || 'N/A'}</td>
      <td>${c.ctc || 'N/A'}</td>
      <td>${c.location || 'N/A'}</td>
      <td>${c.minCgpa || 'N/A'}</td>
      <td>${c.eligibleBranches || 'All'}</td>
      <td>${c.openings || 'N/A'}</td>
    </tr>`;
  });
  html += '</tbody></table>';
  document.getElementById('companiesData').innerHTML = html;
}

function loadApplicationsData() {
  if (applicationsData.length === 0) {
    document.getElementById('applicationsData').innerHTML = '<p class="no-data">No applications yet</p>';
    return;
  }
  
  let html = '<table class="data-table"><thead><tr><th>Student</th><th>Roll</th><th>Company</th><th>Role</th><th>Status</th><th>Action</th></tr></thead><tbody>';
  applicationsData.forEach(a => {
    const statusClass = 'status-' + (a.status || 'applied').toLowerCase();
    html += `<tr>
      <td>${a.student?.name || 'N/A'}</td>
      <td>${a.student?.roll || 'N/A'}</td>
      <td>${a.companyName || 'N/A'}</td>
      <td>${a.jobRole || 'N/A'}</td>
      <td><span class="status-badge ${statusClass}">${a.status || 'Applied'}</span></td>
      <td>
        <select onchange="updateStatus(${a.id}, this.value)" class="action-btn">
          <option value="">Change Status</option>
          <option value="Shortlisted">Shortlisted</option>
          <option value="Interviewed">Interviewed</option>
          <option value="Selected">Selected</option>
          <option value="Rejected">Rejected</option>
        </select>
      </td>
    </tr>`;
  });
  html += '</tbody></table>';
  document.getElementById('applicationsData').innerHTML = html;
}

function updateStatus(appId, newStatus) {
  if (!newStatus) return;
  alert(`Status update: Application ${appId} → ${newStatus}\n(Backend endpoint needed)`);
}

function showAddCompanyForm() {
  const form = document.getElementById('addCompanyForm');
  form.style.display = form.style.display === 'none' ? 'block' : 'none';
}

async function submitCompanyForm(e) {
  e.preventDefault();
  
  const formData = new URLSearchParams();
  formData.append('name', document.getElementById('companyName').value);
  formData.append('hrEmail', document.getElementById('hrEmail').value);
  formData.append('role', document.getElementById('jobRole').value);
  formData.append('ctc', document.getElementById('ctc').value);
  formData.append('location', document.getElementById('location').value);
  formData.append('minCgpa', document.getElementById('minCgpa').value);
  formData.append('branches', document.getElementById('branches').value);
  formData.append('openings', document.getElementById('openings').value);
  formData.append('jobDescription', document.getElementById('jobDescription').value);
  
  try {
    const response = await fetch('/api/companies', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: formData
    });
    
    if (response.ok) {
      alert('✅ Company added successfully!');
      document.getElementById('companyForm').reset();
      document.getElementById('addCompanyForm').style.display = 'none';
      loadDashboardData();
    } else {
      alert('❌ Failed to add company');
    }
  } catch (error) {
    alert('❌ Error: ' + error.message);
  }
}

function switchAdminTab(tabName) {
  document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
  document.querySelectorAll('.tab-content').forEach(t => t.classList.remove('active'));
  
  event.target.classList.add('active');
  document.getElementById(tabName + 'Tab').classList.add('active');
  
  if (tabName === 'students') loadStudentsData();
  else if (tabName === 'companies') loadCompaniesData();
  else if (tabName === 'applications') loadApplicationsData();
}

window.addEventListener('DOMContentLoaded', () => {
  checkAdminAuth();
  const companyForm = document.getElementById('companyForm');
  if (companyForm) {
    companyForm.addEventListener('submit', submitCompanyForm);
  }
});
