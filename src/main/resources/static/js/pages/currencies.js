// 통화 목록을 서버에서 가져와 테이블에 표시하는 함수
function loadCurrencies() {
  // GET 요청으로 서버의 통화 리스트를 가져옴
  $.get('/currencies/list', function (list) {
    // 테이블 tbody(#currency-list) 비우기
    const tbody = $('#currency-list').empty();
    // 받아온 통화 리스트를 순회하며 각 행 생성
    list.forEach(c => {
      const row = `
        <tr>
          <td class="text-center">${c.currencyCode}</td>
          <td class="text-center">${c.exchangeRate}</td>
          <td class="text-center">
            <!-- 수정/삭제 버튼에 해당 통화 정보 data-*로 바인딩 -->
            <button class="btn btn-primary btn-sm edit-btn" data-code="${c.currencyCode}" data-rate="${c.exchangeRate}">수정</button>
            <button class="btn btn-danger btn-sm delete-btn" data-code="${c.currencyCode}">삭제</button>
          </td>
        </tr>
      `;
      // 생성한 행을 tbody에 추가
      tbody.append(row);
    });
  });
}

// 문서 준비 시(jQuery ready) 아래 코드 실행
$(function () {

  // 최초에 통화 목록을 불러옴
  loadCurrencies();

  // 통화 등록 폼 제출 이벤트 핸들러
  $('#currency-form').submit(function (e) {
    e.preventDefault(); // form의 기본 submit 동작(새로고침) 방지

    // 폼 데이터 추출하여 ajax로 서버에 등록 요청
    $.ajax({
      url: '/admin/currencies',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify({
        currencyCode: $('#currency-code').val(),
        exchangeRate: $('#exchange-rate').val()
      }),
      success: function () {
        // 등록 성공 시 통화 목록 갱신 및 폼 리셋
        loadCurrencies();
        $('#currency-form')[0].reset();
      },
      error: function (x) {
        // 에러 발생 시 서버 메시지(alert) 또는 '등록 실패' 알림
        alert((x.responseJSON && x.responseJSON.message) || '등록 실패');
      }
    });
  });

  // 동적으로 만들어진 버튼들의 이벤트 위임 처리
  $('#currency-list')
    // 삭제 버튼 클릭 이벤트
    .on('click', '.delete-btn', function () {
      if (confirm('삭제하시겠습니까?')) {
        // 삭제 요청(DELETE), 성공 시 목록 갱신
        $.ajax({
          url: `/admin/currencies/${$(this).data('code')}`,
          type: 'DELETE',
          success: function () {
            loadCurrencies();
            alert("삭제되었습니다.");
          },
          error: function (x) {
            alert((x.responseJSON && x.responseJSON.message) || '삭제 실패');
          }
        });
      }
    })
    // 수정 버튼 클릭 이벤트
    .on('click', '.edit-btn', function () {
      // 모달 창에 선택한 통화 코드/환율 값 입력
      $('#edit-code').val($(this).data('code'));
      $('#edit-exchange').val($(this).data('rate'));
      // 수정용 모달 창 오픈(Bootstrap Modal)
      $('#currencyEditModal').modal('show');
    });

  // 통화 수정 폼 제출 이벤트 핸들러
  $('#currency-edit-form').submit(function (e) {
    e.preventDefault(); // 폼 기본 동작 방지
    const code = $('#edit-code').val();
    // 서버에 수정 요청 보내기(PUT)
    $.ajax({
      url: `/admin/currencies/${code}`,
      type: 'PUT',
      contentType: 'application/json',
      data: JSON.stringify({
        currencyCode: code,
        exchangeRate: $('#edit-exchange').val()
      }),
      success: function () {
        // 수정 성공 시 모달 닫고, 목록 갱신
        $('#currencyEditModal').modal('hide');
        loadCurrencies();
        alert("수정되었습니다.");
      },
      error: function (x) {
        alert((x.responseJSON && x.responseJSON.message) || '수정 실패');
      }
    });
  });
});
