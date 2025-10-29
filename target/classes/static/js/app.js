// Minimal client JS to call backend endpoints for quick demo
document.addEventListener('DOMContentLoaded', function(){
  const loadStudentsBtn = document.getElementById('loadStudents');
  const studentsOut = document.getElementById('studentsOut');
  const loadCompaniesBtn = document.getElementById('loadCompanies');
  const companiesOut = document.getElementById('companiesOut');
  const filterBtn = document.getElementById('filterEligible');
  const eligibleOut = document.getElementById('eligibleOut');
  const btnSummary = document.getElementById('btnSummary');
  const summaryDiv = document.getElementById('summary');
  const loadAppsBtn = document.getElementById('loadApps');
  const appsOut = document.getElementById('appsOut');

  if (loadStudentsBtn) {
    loadStudentsBtn.onclick = () => fetch('/api/students').then(r=>r.json()).then(data=>{
      studentsOut.textContent = JSON.stringify(data, null, 2);
    });
  }
  if (loadCompaniesBtn) {
    loadCompaniesBtn.onclick = () => fetch('/api/companies').then(r=>r.json()).then(data=>{
      companiesOut.textContent = JSON.stringify(data, null, 2);
    });
  }
  if (filterBtn) {
    filterBtn.onclick = async () => {
      const cid = Number(document.getElementById('companyIdForFilter').value);
      if (!cid) { eligibleOut.textContent = 'Provide company id'; return }
      const [companies, students] = await Promise.all([fetch('/api/companies').then(r=>r.json()), fetch('/api/students').then(r=>r.json())]);
      const comp = companies.find(c=>c.id===cid);
      if (!comp) { eligibleOut.textContent='Company not found'; return }
      const minCgpa = comp.minCgpa || 0;
      const branches = (comp.branches||'').split(',').map(s=>s.trim().toLowerCase()).filter(Boolean);
      const eligible = students.filter(s => (s.cgpa||0) >= minCgpa && (branches.length===0 || branches.includes((s.branch||'').toLowerCase())));
      eligibleOut.textContent = JSON.stringify(eligible, null, 2);
    }
  }
  if (btnSummary) {
    btnSummary.onclick = async () => {
      const [students, companies, apps] = await Promise.all([fetch('/api/students').then(r=>r.json()), fetch('/api/companies').then(r=>r.json()), fetch('/api/applications').then(r=>r.json())]);
      const placed = apps.filter(a=>a.status && a.status.toLowerCase()==='selected').length;
      summaryDiv.innerHTML = `<p>Students: ${students.length}</p><p>Companies: ${companies.length}</p><p>Applications: ${apps.length}</p><p>Selected: ${placed}</p>`;
    }
  }
  if (loadAppsBtn) {
    loadAppsBtn.onclick = () => fetch('/api/applications').then(r=>r.json()).then(data=> appsOut.textContent = JSON.stringify(data, null, 2));
  }
  const downloadBtn = document.getElementById('downloadResume');
  if (downloadBtn) {
    downloadBtn.onclick = async () => {
      const sid = Number(document.getElementById('resumeStudentId').value);
      if (!sid) { alert('enter student id'); return }
      const resp = await fetch('/resume/'+sid, { credentials: 'same-origin' });
      if (!resp.ok) { alert('error: '+resp.status+" - "+await resp.text()); return }
      const blob = await resp.blob();
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'resume_'+sid+'.pdf';
      document.body.appendChild(a);
      a.click();
      a.remove();
      URL.revokeObjectURL(url);
    }
  }
});
