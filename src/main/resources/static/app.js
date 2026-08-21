let selectedCourse = null;
let selectedCollege = null;
let currentAdmissionId = null;
let latestColleges = [];
let latestAdminRows = [];
let currentStudentEmail = '';
let currentAdminEmail = '';

const steps = ['profile', 'courses', 'colleges', 'admission', 'payment', 'confirmation'];
const byId = (id) => document.getElementById(id);

document.addEventListener('click', (event) => {
    const studentModeButton = event.target.closest('[data-student-mode]');
    if (!studentModeButton) return;

    event.preventDefault();
    if (studentModeButton.dataset.studentMode === 'application') {
        openAdmissionApplication();
        return;
    }
    openSelectionStatus();
});

async function api(path, options = {}) {
    const response = await fetch(path, {
        headers: {'Content-Type': 'application/json'},
        ...options
    });
    if (!response.ok) {
        const error = await response.json().catch(() => ({error: 'Request failed'}));
        throw new Error(error.error || 'Request failed');
    }
    return response.json();
}

function setStep(stepName) {
    showStudentMode('application', false);
    steps.forEach((step) => {
        const section = byId(`${step}Step`);
        if (section) {
            section.classList.toggle('active-step', step === stepName);
        }

        const dot = document.querySelector(`[data-step-dot="${step}"]`);
        if (dot) {
            const currentIndex = steps.indexOf(stepName);
            const dotIndex = steps.indexOf(step);
            dot.classList.toggle('active', dotIndex === currentIndex);
            dot.classList.toggle('done', dotIndex < currentIndex);
        }
    });

    const target = byId(`${stepName}Step`);
    if (target) {
        target.scrollIntoView({behavior: 'smooth', block: 'start'});
    }
}

function showStudentMode(modeName, shouldScroll = true) {
    byId('studentActionStep').classList.toggle('active-step', modeName === 'options');
    byId('applicationFlow').classList.toggle('active-step', modeName === 'application');
    byId('meritCheckingStep').classList.toggle('active-step', modeName === 'merit');

    if (shouldScroll) {
        window.scrollTo({top: 0, behavior: 'smooth'});
    }
}

function openAdmissionApplication() {
    if (currentStudentEmail) {
        byId('email').value = currentStudentEmail;
        byId('email').readOnly = true;
    }
    setStep('profile');
}

function openSelectionStatus() {
    byId('meritStatusEmail').value = currentStudentEmail || '';
    byId('meritStatusAdmissionId').value = '';
    byId('studentMeritStatus').className = 'notice';
    byId('studentMeritStatus').textContent = 'Enter your email or admission ID to view selection details.';
    showStudentMode('merit');
}

function showNotice(message, isError = false) {
    byId('agentSummary').className = isError ? 'notice error' : 'notice';
    byId('agentSummary').textContent = message;
}

function setAuthNotice(id, message, isError = false) {
    byId(id).className = isError ? 'notice error' : 'notice';
    byId(id).textContent = message;
}

async function handleStudentAuth(action) {
    const email = byId('studentAuthEmail').value.trim();
    const password = byId('studentAuthPassword').value;
    const button = action === 'register' ? byId('studentRegisterBtn') : byId('studentLoginBtn');
    const originalText = button.textContent;

    try {
        button.disabled = true;
        button.textContent = action === 'register' ? 'Registering...' : 'Logging in...';
        const response = await api(`/api/auth/student/${action}`, {
            method: 'POST',
            body: JSON.stringify({email, password})
        });
        currentStudentEmail = response.email;
        setAuthNotice('studentAuthNotice', response.message);
        byId('email').value = currentStudentEmail;
        byId('email').readOnly = true;
        showPortal('student');
    } catch (error) {
        setAuthNotice('studentAuthNotice', error.message, true);
    } finally {
        button.disabled = false;
        button.textContent = originalText;
    }
}

async function handleAdminLogin() {
    const email = byId('adminAuthEmail').value.trim();
    const password = byId('adminAuthPassword').value;
    const button = byId('adminLoginBtn');

    try {
        button.disabled = true;
        button.textContent = 'Logging in...';
        const response = await api('/api/auth/admin/login', {
            method: 'POST',
            body: JSON.stringify({email, password})
        });
        currentAdminEmail = response.email;
        setAuthNotice('adminAuthNotice', response.message);
        showPortal('admin');
    } catch (error) {
        setAuthNotice('adminAuthNotice', error.message, true);
    } finally {
        button.disabled = false;
        button.textContent = 'Login';
    }
}

function renderCourses(courses) {
    if (!courses.length) {
        window.currentCourses = [];
        byId('courseResults').innerHTML = `
            <div class="notice">
                Sorry, no matching course is currently available for the qualification entered.
                Please check the official Bangalore University notification or enter another recognised degree.
            </div>`;
        return;
    }

    byId('courseResults').innerHTML = courses.map((course, index) => `
        <article class="choice-card">
            <div class="card-top">
                <div>
                    <span class="tag">${course.duration}</span>
                    <h3>${course.courseName}</h3>
                </div>
                <button class="choice-button" type="button" onclick="chooseCourse(${index})">Choose</button>
            </div>
            <p>${course.reason}</p>
            <p><strong>Eligibility:</strong> ${course.eligibility}</p>
            <p class="source-link"><strong>Source:</strong> ${course.source}</p>
        </article>
    `).join('');

    window.currentCourses = courses;
}

function chooseCourse(index) {
    selectedCourse = window.currentCourses[index];
    selectedCollege = null;
    byId('selectedCourse').value = selectedCourse.courseName;
    byId('courseSummary').textContent = `Selected course: ${selectedCourse.courseName} | Duration: ${selectedCourse.duration}`;
    loadBangaloreUniversity();
}

function renderColleges(colleges) {
    latestColleges = colleges;
    byId('collegeResults').innerHTML = colleges.length
        ? colleges.map((college, index) => `
            <article class="choice-card">
                <div class="card-top">
                    <div>
                        <span class="tag">${college.duration}</span>
                        <h3>${college.collegeName}</h3>
                    </div>
                    <button class="choice-button" type="button" onclick="chooseCollege(${index})">Continue</button>
                </div>
                <p><strong>Location:</strong> ${college.location}</p>
                <p><strong>Estimated fees:</strong> Rs. ${college.estimatedFees}</p>
                <p><strong>Course:</strong> ${college.courseAvailability}</p>
                <p><strong>Eligibility:</strong> ${college.eligibility}</p>
                <p><strong>Hostel:</strong> ${college.hostelAvailability}</p>
                <p class="source-link"><strong>Official source:</strong> <a href="${college.officialSource}" target="_blank">${college.officialSource}</a></p>
            </article>
        `).join('')
        : '<div class="notice">Bangalore University fee details could not be loaded for this course.</div>';
}

function chooseCollege(index) {
    selectedCollege = latestColleges[index];
    byId('collegeName').value = selectedCollege.collegeName;
    byId('selectedCollegeType').value = selectedCollege.collegeType;
    byId('fees').value = selectedCollege.estimatedFees;
    byId('collegeSummary').textContent = `Selected university: ${selectedCollege.collegeName} | Estimated fees: Rs. ${selectedCollege.estimatedFees}`;
    setStep('admission');
}

async function loadBangaloreUniversity() {
    if (!selectedCourse) {
        setStep('courses');
        return;
    }

    byId('collegeResults').innerHTML = '<div class="notice">Loading Bangalore University course and fee details...</div>';
    setStep('colleges');

    try {
        const colleges = await api('/api/colleges/search', {
            method: 'POST',
            body: JSON.stringify({
                courseName: selectedCourse.courseName,
                collegeType: 'University',
                location: 'Bengaluru',
                maximumBudget: null
            })
        });
        renderColleges(colleges);
    } catch (error) {
        byId('collegeResults').innerHTML = `<div class="notice error">${error.message}. Please make sure Spring Boot is running.</div>`;
    }
}

byId('analyzeBtn').addEventListener('click', async () => {
    byId('analyzeBtn').disabled = true;
    byId('analyzeBtn').textContent = 'Analyzing...';

    try {
        const data = await api('/api/agent/analyze', {
            method: 'POST',
            body: JSON.stringify({message: byId('studentMessage').value})
        });
        showNotice(data.summary);
        byId('aiStatus').textContent = data.aiEnhanced ? 'OpenAI enhanced guidance' : 'Local admission guidance';
        byId('degree').value = data.extractedDegree === 'Not specified' ? '' : data.extractedDegree;
        byId('percentage').value = data.extractedPercentage || '';
        renderCourses(data.recommendations);
        setStep('courses');
    } catch (error) {
        showNotice('Could not contact the backend. Please start Spring Boot and MySQL, then try again.', true);
    } finally {
        byId('analyzeBtn').disabled = false;
        byId('analyzeBtn').textContent = 'Analyze My Profile';
    }
});

byId('submitAdmissionBtn').addEventListener('click', async () => {
    if (!selectedCourse || !selectedCollege) {
        setStep(selectedCourse ? 'colleges' : 'courses');
        return;
    }

    try {
        if (!byId('policyAccepted').checked) {
            byId('collegeSummary').textContent = 'Please verify and accept the official eligibility, fee, and admission policy checkbox before submitting.';
            return;
        }

        const admission = await api('/api/admissions', {
            method: 'POST',
            body: JSON.stringify({
                student: {
                    name: byId('name').value,
                    email: byId('email').value,
                    phone: byId('phone').value,
                    dateOfBirth: byId('dob').value || null,
                    address: byId('address').value,
                    degree: byId('degree').value,
                    university: byId('university').value,
                    percentage: Number(byId('percentage').value)
                },
                courseName: selectedCourse.courseName,
                collegeName: selectedCollege.collegeName,
                collegeType: selectedCollege.collegeType,
                fees: Number(selectedCollege.estimatedFees),
                duration: selectedCollege.duration,
                collegeSource: selectedCollege.officialSource,
                policyAccepted: byId('policyAccepted').checked
            })
        });
        currentAdmissionId = admission.admissionId;
        byId('payBtn').disabled = false;
        byId('receiptBtn').disabled = true;
        byId('paymentSummary').textContent = `Admission ID ${currentAdmissionId} created. Amount to pay: Rs. ${selectedCollege.estimatedFees}`;
        setStep('payment');
        loadAdmin();
    } catch (error) {
        byId('collegeSummary').textContent = error.message;
    }
});

byId('payBtn').addEventListener('click', async () => {
    if (!currentAdmissionId || !selectedCollege) return;

    byId('payBtn').disabled = true;
    byId('payBtn').textContent = 'Processing...';

    try {
        await api('/api/payments/simulate', {
            method: 'POST',
            body: JSON.stringify({
                admissionId: currentAdmissionId,
                amount: Number(selectedCollege.estimatedFees),
                transactionReference: ''
            })
        });
        const confirmation = await api(`/api/confirmations/${currentAdmissionId}`);
        byId('confirmation').textContent = formatConfirmation(confirmation);
        byId('receiptBtn').disabled = false;
        byId('confirmationReceiptBtn').disabled = false;
        byId('paymentSummary').textContent = 'Payment successful. You can now download the fee receipt.';
        setStep('confirmation');
        loadAdmin();
    } catch (error) {
        byId('paymentSummary').textContent = error.message;
        byId('payBtn').disabled = false;
    } finally {
        byId('payBtn').textContent = 'Simulate Payment';
    }
});

byId('receiptBtn').addEventListener('click', () => {
    if (!currentAdmissionId) return;
    window.location.href = `/api/confirmations/${currentAdmissionId}/receipt`;
});

byId('confirmationReceiptBtn').addEventListener('click', () => {
    if (!currentAdmissionId) return;
    window.location.href = `/api/confirmations/${currentAdmissionId}/receipt`;
});

function formatConfirmation(confirmation) {
    return [
        'Admission Confirmed',
        '',
        `Admission ID: ${confirmation.admissionId}`,
        `Payment ID: ${confirmation.paymentId}`,
        `Student: ${confirmation.studentName}`,
        `Email: ${confirmation.email}`,
        `Course: ${confirmation.courseName}`,
        `University: ${confirmation.collegeName}`,
        `Paid Amount: Rs. ${confirmation.paidAmount}`,
        `Payment Status: ${confirmation.paymentStatus}`,
        `Admission Status: ${confirmation.admissionStatus}`
    ].join('\n');
}

async function loadAdmin() {
    try {
        const query = encodeURIComponent(byId('adminQuery').value);
        const department = encodeURIComponent(byId('departmentFilter').value);
        const rows = await api(`/api/admin/admissions?query=${query}&department=${department}`);
        latestAdminRows = rows;
        updateAdminStats(rows);
        updateMeritCourseOptions(rows);
        byId('adminRows').innerHTML = rows.length ? rows.map((admission) => `
            <tr>
                <td>${admission.admissionId}</td>
                <td>${admission.studentId}</td>
                <td>${admission.studentName}<br><small>${admission.email}</small></td>
                <td>${admission.phone}</td>
                <td>${admission.degree}</td>
                <td>${admission.percentage}</td>
                <td>${admission.department}</td>
                <td>${admission.courseName}</td>
                <td>${admission.collegeName}</td>
                <td>Rs. ${admission.fees || 0}</td>
                <td>${admission.admissionStatus}</td>
            </tr>
        `).join('') : '<tr><td colspan="11">No admission records found.</td></tr>';
    } catch (error) {
        latestAdminRows = [];
        byId('adminRows').innerHTML = '<tr><td colspan="11">Start Spring Boot and MySQL to view saved admission records.</td></tr>';
    }
}

function updateAdminStats(rows) {
    byId('totalStudents').textContent = rows.length;
    byId('computerScienceCount').textContent = rows.filter((row) => row.department === 'Computer Science').length;
    byId('commerceCount').textContent = rows.filter((row) => row.department === 'Commerce').length;
    byId('artsCount').textContent = rows.filter((row) => row.department === 'Arts and Humanities').length;
}

function updateMeritCourseOptions(rows) {
    const selectedValue = byId('meritCourseSelect').value;
    const courses = [...new Set(rows.map((row) => row.courseName).filter(Boolean))].sort();
    byId('meritCourseSelect').innerHTML = '<option value="">Select course</option>'
        + courses.map((course) => `<option value="${course}">${course}</option>`).join('');
    if (courses.includes(selectedValue)) {
        byId('meritCourseSelect').value = selectedValue;
    }
}

async function releaseMeritList() {
    const courseName = byId('meritCourseSelect').value;
    if (!courseName) {
        byId('meritListSummary').textContent = 'Please select a course before releasing the merit list.';
        return;
    }

    try {
        const meritRows = await api(`/api/admin/merit-list/release?courseName=${encodeURIComponent(courseName)}`, {
            method: 'POST'
        });
        byId('meritListSummary').textContent = meritRows.length
            ? `Released merit list: top ${meritRows.length} eligible student(s) for ${courseName}, ranked by highest percentage.`
            : `No eligible students found for ${courseName}.`;
        byId('meritRows').innerHTML = meritRows.length ? meritRows.map((student, index) => `
            <tr>
                <td>${index + 1}</td>
                <td>${student.studentId}</td>
                <td>${student.studentName}<br><small>${student.email}</small></td>
                <td>${student.percentage}</td>
                <td>${student.courseName}</td>
                <td>${student.department}</td>
                <td>${student.collegeName}</td>
                <td>${student.admissionStatus}</td>
            </tr>
        `).join('') : '<tr><td colspan="8">No eligible students found for this course.</td></tr>';
    } catch (error) {
        byId('meritListSummary').textContent = error.message;
        byId('meritRows').innerHTML = '<tr><td colspan="8">Unable to release merit list.</td></tr>';
    }
}

async function checkStudentMeritStatus() {
    const email = byId('meritStatusEmail').value.trim();
    const admissionId = byId('meritStatusAdmissionId').value.trim();
    if (!email && !admissionId) {
        byId('studentMeritStatus').className = 'notice error';
        byId('studentMeritStatus').textContent = 'Please enter your email or admission ID.';
        return;
    }

    const params = new URLSearchParams();
    if (email) params.set('email', email);
    if (admissionId) params.set('admissionId', admissionId);

    try {
        byId('checkMeritStatusBtn').disabled = true;
        byId('checkMeritStatusBtn').textContent = 'Checking...';
        const status = await api(`/api/students/merit-status?${params.toString()}`);
        renderSelectionStatus(status);
    } catch (error) {
        byId('studentMeritStatus').className = 'notice error';
        byId('studentMeritStatus').textContent = error.message;
    } finally {
        byId('checkMeritStatusBtn').disabled = false;
        byId('checkMeritStatusBtn').textContent = 'Check Selection Status';
    }
}

function renderSelectionStatus(status) {
    byId('studentMeritStatus').className = status.selected ? 'notice selection-status success' : 'notice selection-status error';
    if (!status.found) {
        byId('studentMeritStatus').innerHTML = `
            <h3>Selection Details</h3>
            <p>${status.message}</p>
        `;
        return;
    }

    byId('studentMeritStatus').innerHTML = `
        <h3>${status.selected ? 'Selected' : 'Not Selected'}</h3>
        <dl>
            <div><dt>Student</dt><dd>${status.studentName || '-'}</dd></div>
            <div><dt>Course</dt><dd>${status.courseName || '-'}</dd></div>
            <div><dt>Department</dt><dd>${status.department || '-'}</dd></div>
            <div><dt>Percentage</dt><dd>${status.percentage ?? '-'}</dd></div>
            <div><dt>Rank</dt><dd>${status.rankPosition ?? '-'}</dd></div>
            <div><dt>Released At</dt><dd>${status.releasedAt ? new Date(status.releasedAt).toLocaleString() : '-'}</dd></div>
        </dl>
        <p>${status.message}</p>
    `;
}

function showPortal(portalName) {
    if (portalName === 'student' && !currentStudentEmail) {
        portalName = 'student-auth';
    }
    if (portalName === 'admin' && !currentAdminEmail) {
        portalName = 'admin-auth';
    }

    byId('portalStep').classList.toggle('active-step', portalName === 'home');
    byId('studentAuthPortal').classList.toggle('active-step', portalName === 'student-auth');
    byId('adminAuthPortal').classList.toggle('active-step', portalName === 'admin-auth');
    byId('studentPortal').classList.toggle('active-step', portalName === 'student');
    byId('adminPortal').classList.toggle('active-step', portalName === 'admin');

    if (portalName === 'student') {
        showStudentMode('options', false);
    }
    if (portalName === 'admin') {
        loadAdmin();
    }
    window.scrollTo({top: 0, behavior: 'smooth'});
}

function downloadExcel() {
    const query = encodeURIComponent(byId('adminQuery').value);
    const department = encodeURIComponent(byId('departmentFilter').value);
    window.location.href = `/api/admin/admissions/download?query=${query}&department=${department}`;
}

function onClick(id, handler) {
    const element = byId(id);
    if (element) {
        element.addEventListener('click', handler);
    }
}

function onChange(id, handler) {
    const element = byId(id);
    if (element) {
        element.addEventListener('change', handler);
    }
}

onClick('homeBtn', () => showPortal('home'));
onClick('studentPortalBtn', () => showPortal(currentStudentEmail ? 'student' : 'student-auth'));
onClick('adminPortalBtn', () => showPortal(currentAdminEmail ? 'admin' : 'admin-auth'));
onClick('studentLoginBtn', () => handleStudentAuth('login'));
onClick('studentRegisterBtn', () => handleStudentAuth('register'));
onClick('adminLoginBtn', handleAdminLogin);
onClick('applicationProcessBtn', openAdmissionApplication);
onClick('meritCheckingBtn', openSelectionStatus);
onChange('departmentFilter', loadAdmin);
onClick('adminSearchBtn', loadAdmin);
onClick('downloadExcelBtn', downloadExcel);
onClick('releaseMeritBtn', releaseMeritList);
onClick('checkMeritStatusBtn', checkStudentMeritStatus);
document.addEventListener('click', (event) => {
    const backButton = event.target.closest('[data-back-step]');
    if (!backButton) return;

    const targetStep = backButton.dataset.backStep;
    if (targetStep === 'home') {
        showPortal('home');
        return;
    }
    if (targetStep === 'student-options') {
        showStudentMode('options');
        return;
    }
    setStep(targetStep);
});

showPortal('home');
