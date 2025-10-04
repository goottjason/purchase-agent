// '공급업체 등록' 카드의 통화 셀렉트박스에 동적으로 로드하는 함수
function loadCurrenciesSelect(selectId, selected) {
  $.get('/currencies/list', function (list) {
    // selectId로 지정된 셀렉트 박스 요소를 비우고 참조를 얻음
    const select = $(selectId).empty();
    // 받아온 통화 리스트 배열을 순회하며 option 태그를 생성, append
    list.forEach(c => {
      // 현재 선택값(selected)이 있으면 해당 option에 selected 속성 부여
      select.append(
        `<option value="${c.currencyCode}" ${c.currencyCode === selected ? 'selected' : ''}>${c.currencyCode}</option>`
      );
    });
  });
}

// '공급업체 목록' 카드의 테이블에 동적으로 로드하는 함수
function loadSuppliers() {
  $.get('/suppliers/list', function (list) {
    // #supplier-list tbody를 비운다.
    const tbody = $('#supplier-list').empty();
    // 받아온 리스트를 순회하며 테이블 행(tr) 생성 후 tbody에 추가
    list.forEach(s => {
      const row = `
        <tr>
          <td class="text-center">${s.supplierCode}</td>
          <td class="text-center">${s.supplierName}</td>
          <td class="text-center">${s.currencyCode}</td>
          <td class="text-center">
            <!-- 수정/삭제 버튼에 각종 data-* 속성으로 정보 바인딩 -->
            <button class="btn btn-primary btn-sm edit-btn" data-code="${s.supplierCode}" data-name="${s.supplierName}" data-currency="${s.currencyCode}">수정</button>
            <button class="btn btn-danger btn-sm delete-btn" data-code="${s.supplierCode}">삭제</button>
          </td>
        </tr>
      `;
      tbody.append(row);
    });
  });
}

// 문서 ready시 초기화 작업
$(function () {
  // 통화 셀렉트박스 로드(등록 폼)
  loadCurrenciesSelect('#supplier-currency');
  // 공급업체 테이블 로드
  loadSuppliers();

  // 공급업체 등록 폼 제출 시 동작
  $('#supplier-form').submit(function (e) {
    e.preventDefault(); // 기본 form submit 동작 방지(ajax 사용)

    // 폼 데이터 추출 후 ajax로 서버에 POST
    $.ajax({
      url: '/admin/suppliers',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify({
        supplierCode: $('#supplier-code').val(),
        supplierName: $('#supplier-name').val(),
        currencyCode: $('#supplier-currency').val()
      }),
      success: function () {
        // 등록 성공 시 공급업체 목록 다시 로드, 폼 초기화
        loadSuppliers();
        $('#supplier-form')[0].reset();
      },
      error: function (x) {
        // 실패 시 메시지 alert
        alert((x.responseJSON && x.responseJSON.message) || '등록 실패');
      }
    });
  });

  // 테이블에 동적으로 생기는 버튼들 이벤트 위임
  $('#supplier-list')
    // 삭제 버튼 클릭 이벤트 바인딩
    .on('click', '.delete-btn', function () {
      if (confirm('삭제하시겠습니까?')) {
        // DELETE ajax 호출, 성공 시 목록 새로고침
        $.ajax({
          url: `/admin/suppliers/${$(this).data('code')}`,
          type: 'DELETE',
          success: loadSuppliers,
          error: function (x) {
            alert((x.responseJSON && x.responseJSON.message) || '삭제 실패');
          }
        });
      }
    })
    // 수정 버튼 클릭 이벤트 바인딩
    .on('click', '.edit-btn', function () {
      // 모달의 입력 값들을 선택된 공급업체 정보로 채움
      $('#edit-s-code').val($(this).data('code'));
      $('#edit-s-name').val($(this).data('name'));
      // 통화 셀렉트박스(수정용) 동적 로드 및 값 세팅
      loadCurrenciesSelect('#edit-s-currency', $(this).data('currency'));
      // 수정 모달을 띄움(Bootstrap modal)
      $('#supplierEditModal').modal('show');
    });

  // 공급업체 수정 폼 제출('수정 완료') 시 동작
  $('#supplier-edit-form').submit(function (e) {
    e.preventDefault(); // 기본 submit 방지
    const code = $('#edit-s-code').val();
    // PUT ajax로 서버에 수정 데이터 전송
    $.ajax({
      url: `/admin/suppliers/${code}`,
      type: 'PUT',
      contentType: 'application/json',
      data: JSON.stringify({
        supplierName: $('#edit-s-name').val(),
        currencyCode: $('#edit-s-currency').val()
      }),
      success: function () {
        // 모달 닫기 및 공급업체 목록 갱신
        $('#supplierEditModal').modal('hide');
        loadSuppliers();
      },
      error: function (x) {
        alert((x.responseJSON && x.responseJSON.message) || '수정 실패');
      }
    });
  });
});
