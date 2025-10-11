// ================== 유틸 함수 ==================
// 카테고리 트리 테이블 렌더 (DFS)
function renderTreeTable(nodes) {

  const tbody = $('#category-tree-table').empty();

  if(!nodes || nodes.length === 0){
    $('#no-data-message').show();
    return;
  }
  $('#no-data-message').hide();

  // DFS로 각 계층별 tr 렌더!
  function renderNode(node, level, parentId){
    const isExpandable = node.children && node.children.length;
    const rowId = `cat-${node.id}`;
    const parentRow = parentId ? ` class="tree-child of-${parentId}" style="display:none;"` : "";
    const caretClass = isExpandable ? "caret-right tree-toggle" : "";
    tbody.append(`
      <tr id="${rowId}" data-id="${node.id}" 
          data-parent="${parentId || ''}" data-level="${level}"${parentRow}>
        <td class="text-center">
          <input type="checkbox" class="category-check" 
                 data-id="${node.id}" data-name="${node.name}" 
                 data-link="${node.link}" data-path="${node.path}">
        </td>
        <td>
          <span class="tree-indent" style="--level:${level};"></span>
          ${isExpandable ? `<span class="${caretClass}"></span>` : ''}
          <span>${node.name}</span>
        </td>
        <td class="text-center">
          ${node.link
            ? `<a href="${node.link}" target="_blank" 
                  class="btn btn-outline-info btn-sm">바로가기</a>` 
            : '-'}
        </td>
        <td class="text-center">
          <select class="form-control form-control-sm prod-count"
                  style="width: 90px; display:block; margin:0 auto;">
            <option>3</option>
            <option>5</option>
            <option selected>10</option>
            <option>20</option>
            <option>30</option>
          </select>
        </td>
      </tr>
    `);
    if(isExpandable) node.children.forEach(child=>renderNode(child, level+1, node.id));
  }
  nodes.forEach(n=>renderNode(n,0));
}
// 문자열의 바이트 수 계산 함수 (UTF-8)
function getByteLength(str) {
  let byteLength = 0;
  for (let ch of str) {
    const code = ch.codePointAt(0);
    if (code <= 0x7f) byteLength += 1;
    else if (code <= 0x7ff) byteLength += 2;
    else if (code <= 0xffff) byteLength += 3;
    else byteLength += 4;
  }
  return byteLength;
}
// 셀렉트박스 동적 생성
function renderSelectBox(options, selectClass) {
  return `
    <select class="form-control form-control-sm ${selectClass}">
      ${options.map(opt => `<option value="${opt.value}">${opt.text}</option>`).join('')}
    </select>`;
}

// ================ 카테고리 불러오기/트리 렌더 ================
function loadCategoryTree() {
  $('#loading-spinner').show();

  $.ajax({
    url: '/categories/tree', // <-- 계층 펼침 JSON 포맷 endpoint (API 참고)
    type: 'GET',
    data: { keyword: $('#search-input').val().trim() },
    success: function(res) {
      // List<CategoryTreeDto> 반환
      console.log("List<CategoryTreeDto>", res);
      renderTreeTable(res || []);
    },
    error: function(){ alert('목록 로드 실패'); },
    complete: function(){ $('#loading-spinner').hide(); }
  });
}

// ================ DOM이 준비되면 이벤트 바인딩 ================
$(function () {
  // [1-1] 트리 카테고리 로딩 및 트리 렌더
  loadCategoryTree();

  // [1-2] 트리 확장/축소 (caret 클릭)
  $('#category-tree-table').on('click','.tree-toggle',function(){
    const $caret = $(this), $tr = $caret.closest('tr'), currId = $tr.data('id');
    const open = $caret.hasClass('caret-down');
    $caret.toggleClass('caret-down caret-right');
    toggleChildren(currId, !open);

    // 자식 노드 show/hide, 부모 닫힘시 하위 전체 닫음
    function toggleChildren(pid, open){
      $(`#category-tree-table tr.of-${pid}`).each(function(){
        $(this).toggle(open);
        // 만약 부모를 닫으면 하위 전체를 재귀로 닫음
        if(!open) $(this).find('.tree-toggle').removeClass('caret-down').addClass('caret-right');
        if(open && $(this).find('.tree-toggle').hasClass('caret-down'))
          toggleChildren($(this).data('id'), true);
        else if(!open)
          toggleChildren($(this).data('id'), false);
      });
    }
  });

  // [1-3] 검색/초기화
  $('#search-form').submit(function(e){ e.preventDefault(); loadCategoryTree(); });
  $('#reset-button').click(function(){ $('#search-input').val(''); loadCategoryTree(); });

  // [1-4] '기본 상품수 선택' 전체 적용
  $(document).on('change', '#default-count', function() {
    var val = $(this).val();
    // 모든 prod-count select의 값을 변경
    $('#category-tree-table .prod-count').val(val);
  });

  // [2-1] 2단계: 카테고리 선택 후 관련 상품 불러오기
  $('#start-fetch').on('click', function () {

    let categoryTreeDtos = [];

    $('#category-tree-table tr').each(function () {
      if ($(this).find('.category-check').prop('checked')) {
        const $checkbox = $(this).find('.category-check');
        categoryTreeDtos.push({
          id: $checkbox.data('id'),
          name: $checkbox.data('name'),
          link: $checkbox.data('link'),
          path: $checkbox.data('path'),
          children: null,  // 선택된 단일 카테고리이므로 children은 null
          productCount: parseInt($(this).find('.prod-count').val()),
          sortOrder: $('#sort-order').val()
        });
      }
    });

    // 최소한 1개 이상 체크
    if (!categoryTreeDtos.length) { alert('카테고리를 선택하세요!'); return; }

    $('#step2').removeClass('hidden');
    $('#progress-bar').css('width', '0%').text('0%');
    let prog = 0;
    let interval = setInterval(() => {
        if (prog < 98) prog += 3;
        $('#progress-bar').css('width', prog + '%').text(prog + '%');
      }, 200
    );

    $.ajax({
      url: '/product-registration/step2-fetch-products',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(categoryTreeDtos),
      success: function (iherbProductDtos) {
        clearInterval(interval);
        $('#progress-bar').css('width', '100%').text('100%');
        $('#product-table-area').removeClass('hidden');
        const tbody = $('#product-table-body').empty();

        console.log("iherbProductDtos", iherbProductDtos)

        iherbProductDtos.forEach(p => {
          tbody.append(`
            <tr>
              <td class="text-center align-middle">
                <input type="checkbox" class="prod-check" data-id="${p.id}" checked>
              </td>
              <td class="text-center align-middle">
                <img src="${p.imageLinks && p.imageLinks[0] ? p.imageLinks[0] : ''}" width="50">
                </td>
              <td class="text-center align-middle">
                ${p.userCategoryName || ''}
              </td>
              <td class="text-center align-middle">
                ${p.brandName || ''}
              </td>
              <td class="text-left align-middle">${p.displayName || ''}</td>
              <td class="text-center align-middle">${p.discountPrice != null ? p.discountPrice : ''}</td>
              <td class="text-center align-middle">${p.isAvailableToPurchase === true ? '가능' : '불가능'}</td>
              <td class="text-left align-middle">${p.recentActivityMessage ? p.recentActivityMessage : '-'}</td>
            </tr>`);
        });
      },
      error: function(xhr, status, error) {
        clearInterval(interval);
        alert('상품 로드 실패');
      }
    });
  });

  // [2-2] 2단계: 테이블 헤더 체크박스 (전체선택/해제)
  $('#select-all-products').on('change', function () {
    const isChecked = $(this).prop('checked');
    $('#product-table-body .prod-check').prop('checked', isChecked);
  });

  // [2-3] 2단계: 개별 체크박스 변경 시 헤더 체크박스 상태 반영
  $('#product-table-body').on('change', '.prod-check', function () {
    const allChecked = $('#product-table-body .prod-check').length === $('#product-table-body .prod-check:checked').length;
    $('#select-all-products').prop('checked', allChecked);
  });

  // [3-1] 3단계: 선택 상품의 상세정보 준비 및 출력
  $('#go-step3').on('click', function () {

    // 선택한 상품고유번호만 담기
    let selectedIds = [];

    $('#product-table-body .prod-check:checked').each(function () {
      selectedIds.push($(this).data('id')); // ex. [13645, 5253, 432]
    });

    if (!selectedIds.length) { alert('등록할 상품을 선택하세요'); return; }
    $('#step3').removeClass('hidden');

    $.ajax({
      url: '/product-registration/step3-prepare-products',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(selectedIds),
      success: function (productRegistrationRequests) {
        console.log("productRegistrationRequests", productRegistrationRequests);
        // 1. 상품 정보 전역 저장(전역 네임스페이스 또는 모듈 스코프 활용) 최종상품등록할 때 필요
        window.productRegistrationRequests = (productRegistrationRequests || []);
        renderStep3ProductTable(productRegistrationRequests || []);
      }
    });
  });

  // [3-2] 3단계 테이블 렌더링 함수
  function renderStep3ProductTable(productRegistrationRequests) {
    // 분리된 카테고리 SelectBox 옵션(실제 옵션 정의 생략, 기존 코드 참고)
    const smartstoreCategories = [
      { value: "0", text: "--- 반드시 선택 ---"},
      { value: "50013421", text: "기타분말가루" },
      { value: "50018980", text: "기타건강분말" },
      { value: "50001905", text: "꿀" },
      { value: "50002615", text: "기타건강보조식품" },
      { value: "50001921", text: "기타과자" },
      { value: "50001767", text: "기타다이어트식품" },
      { value: "50012241", text: "기타단백질보충제" },
      { value: "50013262", text: "기타소스/드레싱" },
      { value: "50012761", text: "기타기름" },
      { value: "50012041", text: "기타건강/기능성음료" },
      { value: "50012002", text: "기타전통/차음료" },
      { value: "50002265", text: "기타과즙음료" },
      { value: "50002384", text: "기타차" },
      { value: "50002266", text: "원두/생두" },
      { value: "50002606", text: "커피믹스/인스턴트커피" },
      { value: "50014181", text: "기타장류" },
      { value: "50013501", text: "기타잼/시럽" },
      { value: "50012500", text: "기타제과/제빵재료" },
      { value: "50012421", text: "기타조미료" }
    ];
    const elevenstCategories = [
      { value: "0", text: "--- 반드시 선택 ---"},
      { value: "1127298", text: "비타민/미네랄 기타" },
      { value: "1127358", text: "기타 영양제" },
      { value: "1127396", text: "기타 자연추출물" },
      { value: "1127406", text: "어린이용 기타" },
      { value: "1127412", text: "기타 헬스보조제" },
      { value: "1127418", text: "기타 다이어트식품" },
      { value: "1148833", text: "기타식료품" }
    ];

    let tbody = $('#step3-table').empty();

    productRegistrationRequests.forEach(request => {
      let p = request.productDto;
      let korName = p.korName;
      let idx = korName.lastIndexOf(',');
      let korNameNoUnit = idx > -1 ? korName.substring(0, idx).trim() : korName.trim();

      let coupangTitle = `${p.brandName || ''} ${korNameNoUnit}`.trim();

      // 기타 제목 가공
      let etcTitle;
      let unitText = `${p.unitValue || ''}${p.unit || ''}`.trim();
      if (p.packQty == 1) {
        // 1개면 브랜드/상품명 + , + 단위
        etcTitle = `${p.brandName || ''} ${korNameNoUnit}, ${unitText}`.trim();
      } else {
        // 2개 이상이면 (N개) 브랜드/상품명 + , + 단위
        etcTitle = `(${p.packQty}개) ${p.brandName || ''} ${korNameNoUnit}, ${unitText}`.trim();
      }

      tbody.append(`
        <tr>
          <!-- 체크박스 (기본: 선택됨) -->
          <td class="text-center align-middle" style="text-align: center;">
            <input type="checkbox" class="submit-final-check" data-code="${p.code}" checked>
          </td>
          <!-- 한글상품명(Title) | 영문상품명) -->
          <td class="text-left align-middle">
            <div class="input-with-count" style="position:relative;">
              <input class="form-control form-control-sm kor-name-input" value="${korNameNoUnit}">
            </div>
            <div style="margin-top:2px;">
              <div class="input-with-count" style="position:relative;">
                <input class="form-control form-control-sm eng-name-input" value="${p.engName || ''}" style="padding-right:34px;">
                <span class="eng-byte byte-badge" style="position:absolute; right:10px; top:50%; pointer-events:none;
                                                  transform:translateY(-50%); font-size:13px;
                                                  background:#eef; color:#236ab9; border-radius:4px;">
                  ${getByteLength(p.engName || '')}
                </span>
              </div>
            </div>
            <div style="margin-top:12px; display:flex; align-items:center;">
              <span style="font-weight:500;">쿠팡 상품명 :</span>
              <span class="coupang-title" style="margin-left:8px;">${coupangTitle}</span>
              <span class="coupang-byte byte-badge" style="margin-left:14px; background:#fffbe6; color:#288f39; border-radius:4px; font-size:13px; padding:2px 6px;">
                ${getByteLength(coupangTitle)}
              </span>
            </div>
            <div style="margin-top:8px; display:flex; align-items:center;">
              <span style="font-weight:500;">기타 상품명 :</span>
              <span class="etc-title" style="margin-left:8px;">${etcTitle}</span>
              <span class="etc-byte byte-badge" style="margin-left:14px; background:#eef; color:#236ab9; border-radius:4px; font-size:13px; padding:2px 6px;">
                ${getByteLength(etcTitle)}
              </span>
            </div>
          </td>
          <!-- 브랜드명/단위 -->
          <td class="text-center align-middle" style="vertical-align: middle;">
            <!-- 전체 wrapper로 폭 통일 -->
            <div style="position:relative;">
              <!-- 브랜드명 -->
              <input class="form-control form-control-sm brand-name-input mb-2" value="${p.brandName}" style="font-size:1rem;">
              <!-- 단위값/단위 2분할 flex -->
              <div class="d-flex" style="gap:6px;">
                <input class="form-control form-control-sm unit-value-input text-center" 
                       value="${p.unitValue}" style="flex:1; font-size:1rem;">
                <input class="form-control form-control-sm unit-input text-center" 
                       value="${p.unit}" style="flex:1; font-size:1rem;">
              </div>
            </div>
          </td>
          <!-- 구매가/묶음수/판매가 -->
          <td class="text-center align-middle">
            <div style="position:relative;">
              <input class="form-control form-control-sm text-right buy-price-input" value="${p.buyPrice}">
            </div>
            <div style="margin-top:2px;">
              <input class="form-control form-control-sm text-right pack-qty-input" value="${p.packQty}">
            </div>
            <div style="margin-top:2px;">
              <input class="form-control form-control-sm text-right sale-price-input" value="${p.salePrice}">
            </div>
          </td>
          <!-- 스마트스토어/11번가 -->
          <td class="text-center align-middle">
            <div style="position:relative;">
              <!-- select박스 넣기 -->
              ${renderSelectBox(smartstoreCategories, 'smartstore-category-select')}
            </div>
            <div style="margin-top:2px;">
              <!-- select박스 넣기 -->
              ${renderSelectBox(elevenstCategories, 'elevenst-category-select')}
            </div>
          </td>
          <!-- 링크 -->
          <td class="text-center align-middle">
            <a href="${p.link}" target="_blank" class="btn btn-outline-info btn-sm" style="font-size:13px;">바로가기</a>
          </td>
        </tr>
      `);
    });
  }

  // [3-3] korName, engName, brandName, unit, unitValue, packQty, buyPrice, marginRate 입력값 변화 시,
  // 쿠팡상품명, 기타상품명, eng-byte, coupang-byte, etc-byte, salePrice 변경
  $(document).on('input',
    '.kor-name-input, .eng-name-input, .brand-name-input, .unit-input, .unit-value-input, ' +
    '.pack-qty-input, .buy-price-input, #global-margin-rate', function () {

    const $tr = $(this).closest('tr');

    const korName = $tr.find('.kor-name-input').val() || '';
    const engName = $tr.find('.eng-name-input').val() || '';
    const brandName = $tr.find('.brand-name-input').val() || '';
    const unit = $tr.find('.unit-input').val() || '';
    const unitValue = $tr.find('.unit-value-input').val() || '';

    const packQty = parseInt($tr.find('.pack-qty-input').val(), 10) || 1;
    const buyPrice = parseFloat($tr.find('.buy-price-input').val()) || 0;
    const marginRate = parseFloat($('#global-margin-rate').val()) || 0;


    // 쿠팡상품명
    let coupangTitle = `${brandName} ${korName}`.trim();
    // 기타상품명
    let unitText = `${unitValue}${unit}`.trim();
    let etcTitle;
    if (packQty === 1) {
      etcTitle = `${brandName} ${korName}, ${unitText}`.trim();
    } else {
      etcTitle = `(${packQty}개) ${brandName} ${korName}, ${unitText}`.trim();
    }
    // 반영: 텍스트
    $tr.find('.coupang-title').text(coupangTitle);
    $tr.find('.etc-title').text(etcTitle);
    // 반영: 바이트수
    $tr.find('.eng-byte').text(`${getByteLength(engName)}`);
    $tr.find('.coupang-byte').text(`${getByteLength(coupangTitle)}`);
    $tr.find('.etc-byte').text(`${getByteLength(etcTitle)}`);

    // 판매가 계산: (구매가 * (1 + 마진율)) * 묶음수 등
    let salePrice = Math.floor(Math.round(buyPrice * (1 + ((marginRate / 100) ?? 0)) * packQty) / 100) * 100;
    // 반영: 판매가
    $tr.find('.sale-price-input').val(salePrice);
  });

  // [3-4] 3단계 테이블 헤더 체크박스 (전체선택/해제)
  $('#select-all-final').on('change', function () {
    const isChecked = $(this).prop('checked');
    $('#step3-table .submit-final-check').prop('checked', isChecked);
  });
  // [3-5] 3단계 개별 체크박스 변경 시 헤더 체크박스 상태 반영
  $('#step3-table').on('change', '.submit-final-check', function () {
    const allChecked = $('#step3-table .submit-final-check').length === $('#step3-table .submit-final-check:checked').length;
    $('#select-all-final').prop('checked', allChecked);
  });

  // [4-1] 상품 최종 등록(4단계)
  $('#submit-final').on('click', function () {

    let productRegistrationRequests = [];

    $('#step3-table tr').each(function () {

      let $cb = $(this).find('.submit-final-check');
      // 체크된 상품만 담기
      if ($cb.prop('checked')) {

        // 코드정보 불러오기
        let code = $cb.data('code');

        // 해당 코드 정보를 window.productRegistrationRequests에서 찾기
        let origin = (window.productRegistrationRequests || []).find(request => request.productDto.code === code);
        if (!origin) return; // 혹시 없는 경우 skip

        // 기존 정보(origin)를 불러온 후, 유저의 최종 수정정보를 반영하여 productRegistrationDtos에 담기
        productRegistrationRequests.push({
          ...origin,
          productDto: {
            ...origin.productDto,
            title: $(this).find('.etc-title').text(),
            korName: $(this).find('.kor-name-input').val(),
            engName: $(this).find('.eng-name-input').val(),
            brandName: $(this).find('.brand-name-input').val(),
            unitValue: $(this).find('.unit-value-input').val(),
            unit: $(this).find('.unit-input').val(),
            buyPrice: $(this).find('.buy-price-input').val(),
            packQty: $(this).find('.pack-qty-input').val(),
            salePrice: $(this).find('.sale-price-input').val(),
            marginRate: parseFloat($('#global-margin-rate').val()),
            // origin에서 다른 필요한 ProductDto 필드들 복사
          },
          smartstoreCategoryId: $(this).find('.smartstore-category-select').val(),
          elevenstCategoryId: $(this).find('.elevenst-category-select').val()
        });
      }
    });
    if (!productRegistrationRequests.length) { alert('등록할 상품을 선택하세요'); return; }

    $.ajax({
      url: '/product-registration/step4-submit-products',
      type: 'POST',
      contentType: 'application/json',
      data: JSON.stringify(productRegistrationRequests),
      success: function (map) {
        console.log(map);
        $('#doneModal').modal('show');
      }
    });
  });
});