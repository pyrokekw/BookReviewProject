// Базовые функции для всего приложения
document.addEventListener('DOMContentLoaded', function() {
    // Инициализация tooltips
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });

    // Автоматическое скрытие alert через 5 секунд
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        setTimeout(() => {
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        }, 5000);
    });

    // Плавная прокрутка для anchor ссылок
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            const target = document.querySelector(this.getAttribute('href'));
            if (target) {
                target.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });

    // === УЛУЧШЕННЫЙ КОД ДЛЯ СОХРАНЕНИЯ ПОЗИЦИИ ===

    // Сохраняем позицию прокрутки и ID элемента перед отправкой формы
    document.querySelectorAll('form').forEach(form => {
        form.addEventListener('submit', function() {
            // Сохраняем текущую позицию прокрутки
            const currentPosition = window.pageYOffset || document.documentElement.scrollTop;
            sessionStorage.setItem('scrollPosition', currentPosition);

            // Находим ближайшую рецензию (если форма внутри рецензии)
            const reviewCard = this.closest('[id^="review-"]');
            if (reviewCard) {
                sessionStorage.setItem('targetElementId', reviewCard.id);
            } else if (currentPosition > 500) { // Если прокрутили достаточно далеко
                // Сохраняем секцию рецензий
                sessionStorage.setItem('targetElementId', 'reviews-section');
            }
        });
    });

    // Восстанавливаем позицию прокрутки после загрузки страницы
    const savedPosition = sessionStorage.getItem('scrollPosition');
    const targetElementId = sessionStorage.getItem('targetElementId');

    if (savedPosition) {
        setTimeout(() => {
            if (targetElementId) {
                // Пытаемся найти конкретный элемент
                const targetElement = document.getElementById(targetElementId);
                if (targetElement) {
                    targetElement.scrollIntoView({
                        behavior: 'smooth',
                        block: 'start'
                    });
                } else {
                    // Если элемент не найден, используем сохраненную позицию
                    window.scrollTo(0, parseInt(savedPosition));
                }
            } else {
                // Просто восстанавливаем позицию
                window.scrollTo(0, parseInt(savedPosition));
            }

            // Очищаем storage
            sessionStorage.removeItem('scrollPosition');
            sessionStorage.removeItem('targetElementId');
        }, 100);
    }

    // Обработка якорей в URL (если нет сохраненной позиции)
    if (window.location.hash && !savedPosition) {
        setTimeout(() => {
            const targetElement = document.querySelector(window.location.hash);
            if (targetElement) {
                targetElement.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        }, 150);
    }

    // Анимация кнопок (уже есть у тебя)
    document.querySelectorAll('.action-btn').forEach(btn => {
        btn.addEventListener('click', function(e) {
            this.style.transform = 'scale(0.95)';
            setTimeout(() => {
                this.style.transform = '';
            }, 150);
        });
    });
});