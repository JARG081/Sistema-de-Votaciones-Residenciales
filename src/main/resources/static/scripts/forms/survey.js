
function survey(addAnswer,answerContainer,mb) {
    const d = document;
    d.querySelector
    d.addEventListener('click', (e)=>{
        if(e.target.matches(addAnswer) || e.target.matches(`${addAnswer} *`)){
            console.log("hola")
            let newAnswer = d.querySelector(answerContainer)
            let newInput = document.createElement('div');
            newInput.classList.add('input-group',`${mb}`)
            newInput.innerHTML = `<input type="text" class="form-control" placeholder="Respuesta" name="respuestas">
            <button class="btn btn-danger eliminarRespuesta" type="button">Eliminar</button>`;
            newAnswer.appendChild(newInput);
        }
        if (e.target.matches('.eliminarRespuesta')) {
            console.log("funciona")
            e.target.closest('.input-group').remove();
        }


    });
}

function showSuervey(showFormBtn=undefined,surveyCard=undefined,hideFormBtn=undefined,surveyForm=undefined){
    const ncard = document.querySelector('.card');
    const nsurvey = document.querySelector('.surveyForm')
    const nshowFormBtn = document.querySelector(showFormBtn);
    const nsurveyCard = document.querySelector(surveyCard);
    const nhideFormBtn = document.querySelector(hideFormBtn);
    const nsurveyForm = document.querySelector(surveyForm);
    nshowFormBtn.addEventListener('click', () => {
            nsurveyForm.style.display = 'block';
            nsurvey.classList.add("card");
            nshowFormBtn.style.display = 'none';
            nsurveyCard.style.display = 'none';
            nhideFormBtn.style.display = 'block';
            ncard.style.style.display = 'none';
        });
        
        nhideFormBtn.addEventListener('click', () => {
            nsurveyForm.style.display = 'none';
            nsurvey.classList.remove("card");
        nshowFormBtn.style.display = 'block';
        nsurveyCard.style.display = 'block';
        nhideFormBtn.style.display = 'none';
    });
}
export {survey,showSuervey}