/** 동작 흐름 상세 설명
 * 1. "수정" 버튼 클릭
 * 현재 폼의 모든 데이터를 originalData에 저장
 * toggleEditMode(true) 호출 → 폼에 .editing 클래스 추가 + 버튼 show/hide
 * view-mode(텍스트) 숨기고 edit-mode(input/textarea)만 노출됨
 * 줄간격/레이아웃 변동 없이 텍스트 ↔ 인풋만 전환
 *
 * 2. "취소" 버튼 클릭
 * confirm으로 정말 취소할지 사용자에게 확인
 * OK 시 restoreOriginalData() 수행(이전 값으로 인풋/textarea 값 복원)
 * 편집모드 해제 (toggleEditMode(false))
 * view-mode(텍스트) 노출
 *
 * 3. "저장" 버튼 클릭
 * validateForm()으로 값 체크 (필수/숫자 유효성 검증)
 * 값이 올바르면 변경사항을 checkForChanges()로 diff
 * 가격/재고가 변경된 경우 안내 후, AJAX로 hasPriceOrStockChange=true 값과 함께 서버 전송
 * 기타 값만 바뀐 경우 안내 없이 AJAX로 전송
 * 변경사항 자체가 없으면 편집모드 해제 및 안내
 *
 * 4. 주요 내부 함수
 * toggleEditMode(isEdit):
 *   폼에 .editing 클래스를 붙이거나 떼어 view/edit모드를 일괄 전환. 버튼 show/hide도 이 함수에서 처리.
 * getFormData():
 *   인풋/텍스트에어리어 값을 name/value 쌍 객체로 수집.
 * restoreOriginalData():
 *   복원 요청 시 이전 데이터로 입력값 원복.
 * checkForChanges():
 *   가격/재고와 기타 값 별로 변경 사항을 판단해, AJAX 전송 정책 분기.
 * validateForm():
 *   입력값 미입력 또는 숫자 값 오류 시 Bootstrap 에러 하이라이트(is-invalid 적용) + 동작 중단.
 * submitForm():
 *   FormData로 폼값 + 가격/재고변경여부를 서버로 전송. 성공 시 리로드, 실패 시 에러 안내.
 *
 * 5. 디자인/동작 특성
 * 버튼 클릭만으로 레이아웃/간격 변화 없이 자연스러운 뷰 <-> 수정 UI 전환
 * 모든 변경 감지, 상태 복원, 유효성, AJAX까지 완전하게 독립적으로 처리
 * 인풋/텍스트 필드 Focus, 값 검증, UI 안내 등 UX 품질 책임
 */


// -------------------------
// 주요 함수 정의
// -------------------------
/**
 * 편집 모드 토글
 * isEdit=true: 편집모드(폼에 .editing 클래스 추가, 버튼 전환)
 * isEdit=false: 읽기모드(.editing 제거)
 */
function toggleEditMode(isEdit) {
  const $form = $('#productForm');
  if (isEdit) {
    $form.addClass('editing');                 // edit-mode만 보이게 (저장, 취소 버튼 보이게)
    $('#editBtn').hide();
    $('#saveBtn, #cancelBtn').show();
  } else {
    $form.removeClass('editing');              // view-mode만 보이게 (수정 버튼 보이게
    $('#editBtn').show();
    $('#saveBtn, #cancelBtn').hide();
  }
}

/**
 * 현재 폼 데이터를 {name: value} 객체로 반환
 * - input, textarea 등 모든 입력값을 탐색
 * - 복원/변경감지 등에 사용
 */
function getFormData() {
  const data = {};
  $('#productForm input, #productForm textarea').each(function() {
    data[$(this).attr('name')] = $(this).val();
  });
  return data;
}

/**
 * 변경사항 감지
 * - 가격/재고가 달라졌는지
 * - 기타 값이 달라졌는지
 * (버튼별 안내/동작 분기)
 */
function checkForChanges(original, current) {
  let priceChanged = false, stockChanged = false, otherChanged = false;

  if (original['salePrice'] !== current['salePrice']) priceChanged = true;
  if (original['stock'] !== current['stock']) stockChanged = true;

  for (let key in current) {
    if (original[key] !== current[key] && !['salePrice','stock'].includes(key)) {
      otherChanged = true;
    }
  }

  return {
    priceChanged,
    stockChanged,
    otherChanged
  };
}

/**
 * 데이터 복원(취소 시)
 * - 저장된 originalData 값을 입력값으로 되돌림
 */
function restoreOriginalData() {
  for (let key in originalData) {
    $(`[name="${key}"]`).val(originalData[key]);
  }
}

/**
 * 폼 유효성 검사
 * - 필수값: title, salePrice
 * - 숫자 유효성: salePrice, stock, buyPrice
 * - 올바르지 않으면 .is-invalid 적용(bootstrap)
 */
function validateForm() {
  let isValid = true;

  // 필수 입력 체크
  const requiredFields = ['title', 'salePrice'];
  requiredFields.forEach(field => {
    const $field = $(`[name="${field}"]`);
    if (!$field.val().trim()) {
      $field.addClass('is-invalid');
      isValid = false;
    } else {
      $field.removeClass('is-invalid');
    }
  });

  // 숫자 필드 값 체크
  const numberFields = ['salePrice', 'stock', 'buyPrice'];
  numberFields.forEach(field => {
    const $field = $(`[name="${field}"]`);
    const value = $field.val();
    if (value && (isNaN(value) || parseFloat(value) < 0)) {
      $field.addClass('is-invalid');
      isValid = false;
    } else {
      $field.removeClass('is-invalid');
    }
  });

  if (!isValid) {
    alert('입력값을 확인해주세요.');
  }

  return isValid;
}

/**
 * 폼 데이터 서버로 제출(AJAX)
 * - 가격/재고 변경여부 파라미터 추가
 * - 성공 시 reload, 실패 시 alert
 */
function submitForm(priceChanged, stockChanged) {
  const formData = new FormData($('#productForm')[0]);
  formData.append('priceChanged', priceChanged);
  formData.append('stockChanged', stockChanged);

  $.ajax({
    url: $('#productForm').attr('action'),
    type: 'PUT',
    data: formData,
    processData: false,
    contentType: false,
    success: function(response) {
      alert('상품이 성공적으로 업데이트되었습니다.');
      location.reload();
    },
    error: function(xhr) {
      alert('업데이트 중 오류가 발생했습니다: ' + (xhr.responseJSON?.message || '알 수 없는 오류'));
    }
  });
}


// -------------------------
// 문서 준비 완료 시 이벤트 바인딩
// -------------------------
$(document).ready(function() {
  // 화면상 원본 데이터를 보관하는 전역 변수
  let originalData = {};

  // -------------------------
  // 수정 버튼 클릭 이벤트 핸들러
  // -------------------------
  $('#editBtn').click(function() {
    // 현재 폼의 데이터를 모두 저장 (취소 시 복원용)
    originalData = getFormData();
    // 편집 모드로 전환
    toggleEditMode(true);
  });

  // -------------------------
  // 저장 버튼 클릭 이벤트 핸들러
  // -------------------------
  $('#saveBtn').click(function() {
    // 유효성 검사
    if (validateForm()) {
      const currentData = getFormData();
      // 변경사항 diff 확인 (가격/재고 vs 기타)
      const changes = checkForChanges(originalData, currentData);

      if (changes.priceChanged || changes.stockChanged) {
        // 안내 메시지는 둘 중 하나라도 변경되면 띄우고,
        if (confirm('가격 또는 재고가 변경되었습니다. 연결된 판매 채널에 업데이트 요청이 발송됩니다. 계속하시겠습니까?')) {
          submitForm(changes.priceChanged, changes.stockChanged);  // 두 값 모두 전달
        }
      } else if (changes.otherChanged) {
        submitForm(false, false); // 기타 변경
      } else {
        alert('변경된 내용이 없습니다.');
        toggleEditMode(false);
      }
    }
  });

  // -------------------------
  // 취소 버튼 클릭 이벤트 핸들러
  // -------------------------
  $('#cancelBtn').click(function() {
    if (confirm('수정을 취소하시겠습니까? 변경사항이 저장되지 않습니다.')) {
      // 원본 데이터로 복원
      restoreOriginalData();
      // 편집 모드 해제
      toggleEditMode(false);
    }
  });
});
