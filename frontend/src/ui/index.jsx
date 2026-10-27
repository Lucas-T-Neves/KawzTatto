import { useId, useLayoutEffect, useRef } from "react"
import { ChevronDown, X } from "lucide-react"
import useAnchoredPopover from "../shared/viewmodel/useAnchoredPopover.js"
import styles from "./Controls.module.css"

const classes = (...values) => values.filter(Boolean).join(" ")

export function Button({
  as: Component = "button",
  variant = "primary",
  size = "medium",
  iconStart,
  iconEnd,
  children,
  className,
  type = "button",
  ...props
}) {
  return (
    <Component
      {...props}
      {...(Component === "button" ? { type } : {})}
      data-ui="button"
      data-variant={variant}
      className={classes(
        styles.button,
        styles[variant],
        styles[size],
        className,
      )}
    >
      {iconStart && (
        <span className={styles.iconSlot} aria-hidden="true">
          {iconStart}
        </span>
      )}
      <span className={styles.buttonContent}>{children}</span>
      {iconEnd && (
        <span className={styles.iconSlot} aria-hidden="true">
          {iconEnd}
        </span>
      )}
    </Component>
  )
}

export function IconButton({
  icon,
  variant = "primary",
  size = "medium",
  className,
  type = "button",
  ...props
}) {
  return (
    <button
      {...props}
      type={type}
      data-ui="icon-button"
      data-variant={variant}
      className={classes(
        styles.button,
        styles.iconButton,
        styles[variant],
        size === "small" ? styles.iconSmall : styles.iconMedium,
        className,
      )}
    >
      {icon}
    </button>
  )
}

export function InputField({
  value,
  onChange,
  label,
  description,
  prefix,
  suffix,
  disabled,
  className,
  id,
  type = "text",
  ...props
}) {
  const generatedId = useId()
  const inputId = id || generatedId
  const descriptionId = description ? `${inputId}-description` : undefined
  return (
    <div
      className={classes(
        styles.fieldRoot,
        disabled && styles.fieldDisabled,
        className,
      )}
    >
      <div className={styles.fieldStack}>
        {label && (
          <label htmlFor={inputId} className={styles.label}>
            {label}
          </label>
        )}
        <div className={styles.controlFrame}>
          {prefix && (
            <span className={styles.fieldAdornment} aria-hidden="true">
              {prefix}
            </span>
          )}
          <input
            {...props}
            id={inputId}
            type={type}
            disabled={disabled}
            className={styles.input}
            value={type === "file" ? undefined : (value ?? "")}
            aria-describedby={props["aria-describedby"] || descriptionId}
            onChange={(event) => onChange?.(event.target.value)}
          />
          {suffix && (
            <span className={styles.fieldAdornment} aria-hidden="true">
              {suffix}
            </span>
          )}
        </div>
        {description && (
          <span id={descriptionId} className={styles.description}>
            {description}
          </span>
        )}
      </div>
    </div>
  )
}

export function TextareaField({
  value = "",
  onChange,
  label,
  description,
  disabled,
  className,
  id,
  rows = 3,
  ...props
}) {
  const generatedId = useId()
  const inputId = id || generatedId
  const descriptionId = description ? `${inputId}-description` : undefined
  return (
    <div
      className={classes(
        styles.fieldRoot,
        disabled && styles.fieldDisabled,
        className,
      )}
    >
      <div className={styles.fieldStack}>
        {label && (
          <label htmlFor={inputId} className={styles.label}>
            {label}
          </label>
        )}
        <textarea
          {...props}
          id={inputId}
          disabled={disabled}
          rows={rows}
          aria-describedby={props["aria-describedby"] || descriptionId}
          className={styles.textarea}
          value={value}
          onChange={(event) => onChange?.(event.target.value)}
        />
        {description && (
          <span id={descriptionId} className={styles.description}>
            {description}
          </span>
        )}
      </div>
    </div>
  )
}

export function SelectField({
  label,
  description,
  options = [],
  value = "",
  onChange,
  placeholder = "Selecione",
  disabled,
  className,
}) {
  const { id, root, popup, open, toggle, close, popupProps } =
    useAnchoredPopover({ matchTriggerWidth: true })
  const current = options.find((option) => option.value === value)
  const show = () => {
    if (disabled) return
    if (open) {
      close()
      return
    }
    toggle()
    requestAnimationFrame(() =>
      popup.current?.querySelector('[aria-selected="true"]')?.focus(),
    )
  }
  return (
    <div
      ref={root}
      className={classes(
        styles.fieldRoot,
        disabled && styles.fieldDisabled,
        className,
      )}
    >
      <div className={styles.fieldStack}>
        {label && (
          <span id={`${id}-label`} className={styles.label}>
            {label}
          </span>
        )}
        <Button
          variant="neutral"
          className={styles.selectTrigger}
          disabled={disabled}
          role="combobox"
          aria-expanded={open}
          aria-controls={id}
          aria-haspopup="listbox"
          aria-label={label ? undefined : placeholder}
          aria-labelledby={label ? `${id}-label ${id}-value` : undefined}
          aria-describedby={description ? `${id}-description` : undefined}
          iconEnd={<ChevronDown className={styles.controlIcon} />}
          onClick={show}
          onKeyDown={(event) => {
            if (event.key === "ArrowDown" || event.key === "ArrowUp") {
              event.preventDefault()
              if (!open) show()
            }
          }}
        >
          <span id={`${id}-value`}>{current?.label || placeholder}</span>
        </Button>
        {description && (
          <span id={`${id}-description`} className={styles.description}>
            {description}
          </span>
        )}
      </div>
      <div
        {...popupProps}
        role="listbox"
        aria-label={label || placeholder}
        className={styles.selectPopover}
        onKeyDown={(event) => {
          popupProps.onKeyDown(event)
          const buttons = [
            ...(popup.current?.querySelectorAll('[role="option"]') || []),
          ]
          if (event.key === "Home" || event.key === "End") {
            event.preventDefault()
            buttons[event.key === "Home" ? 0 : buttons.length - 1]?.focus()
          } else if (event.key === "Tab") close()
          else if (event.key.length === 1 && event.key !== " ") {
            const index = options.findIndex((option) =>
              option.label
                .toLocaleLowerCase("pt-BR")
                .startsWith(event.key.toLocaleLowerCase("pt-BR")),
            )
            if (index >= 0) {
              event.preventDefault()
              buttons[index]?.focus()
            }
          }
        }}
      >
        {options.length ? (
          options.map((option) => (
            <Button
              key={option.value}
              size="small"
              variant="subtle"
              role="option"
              className={styles.selectOption}
              aria-selected={option.value === value}
              onClick={() => {
                onChange(option.value)
                close()
              }}
            >
              {option.label}
            </Button>
          ))
        ) : (
          <span className={styles.description}>Nenhuma opção disponível.</span>
        )}
      </div>
    </div>
  )
}

export function Modal({
  isOpen,
  onClose,
  title,
  children,
  footer,
  size = "medium",
  className,
}) {
  const dialog = useRef(null)
  const titleId = useId()
  useLayoutEffect(() => {
    const element = dialog.current
    if (isOpen && element && !element.open) element.showModal()
    if (!isOpen && element?.open) element.close()
  }, [isOpen])
  useLayoutEffect(
    () => () => {
      if (dialog.current?.open) dialog.current.close()
    },
    [],
  )
  return (
    <dialog
      ref={dialog}
      className={classes(styles.dialog, styles[`modal${size}`])}
      aria-labelledby={title ? titleId : undefined}
      aria-label={title ? undefined : "Detalhes"}
      onCancel={(event) => {
        event.preventDefault()
        onClose()
      }}
      onClick={(event) => {
        if (event.target === event.currentTarget) onClose()
      }}
    >
      {isOpen && (
        <section className={classes(styles.modalPanel, className)}>
          <div className={styles.modalHeader}>
            <div
              id={titleId}
              role="heading"
              aria-level={2}
              className={styles.modalTitle}
            >
              {title}
            </div>
            <IconButton
              variant="subtle"
              size="small"
              aria-label="Fechar janela"
              onClick={onClose}
              icon={<X className={styles.controlIcon} />}
            />
          </div>
          <div className={styles.modalBody}>{children}</div>
          {footer && <div className={styles.modalFooter}>{footer}</div>}
        </section>
      )}
    </dialog>
  )
}

export function SidebarNavigation({ children, footer, className, ...props }) {
  return (
    <nav
      {...props}
      aria-label={props["aria-label"] || "Navegação principal"}
      className={classes(styles.sidebar, className)}
    >
      <div className={styles.sidebarContent}>{children}</div>
      {footer && <div className={styles.sidebarFooter}>{footer}</div>}
    </nav>
  )
}
