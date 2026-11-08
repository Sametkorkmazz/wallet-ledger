import { signUpSchema, type SignUpValues } from "@/schemas/auth";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { useNavigate } from "react-router-dom";
import { Button } from "../ui/button";
import { Input } from "../ui/input";
import { Label } from "../ui/label";

export default function SignUpForm() {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<SignUpValues>({
    resolver: zodResolver(signUpSchema),
  });

  const navigate = useNavigate();
  function onSubmit() {
    // TODO: call api.signUp(data)
    navigate("/dashboard");
  }

  return (
    <form
      className="flex flex-col gap-5"
      noValidate
      onSubmit={handleSubmit(onSubmit)}
    >
      <div className="flex flex-col gap-1.5">
        <Label htmlFor="su-email">Email</Label>
        <Input
          aria-invalid={!!errors.email}
          placeholder="you@example.com"
          autoComplete="email"
          id="su-email"
          {...register("email")}
        />
        {errors.email && (
          <p className="text-sm text-danger">{errors.email.message}</p>
        )}
      </div>

      <div className="flex flex-col gap-1.5">
        <Label htmlFor="su-password">Password</Label>
        <Input
          aria-invalid={!!errors.password}
          autoComplete="new-password"
          type="password"
          id="su-password"
          {...register("password")}
        />
        {!errors.password && (
          <span className="text-sm text-muted-foreground">
            10-72 characters
          </span>
        )}
        {errors.password && (
          <p className="text-sm text-danger">{errors.password.message}</p>
        )}
      </div>

      <div className="flex flex-col gap-1.5">
        <Label htmlFor="su-confirm-password">Confirm password</Label>
        <Input
          aria-invalid={!!errors.confirmPassword}
          autoComplete="new-password"
          type="password"
          id="su-confirm-password"
          {...register("confirmPassword")}
        />
        {errors.confirmPassword && (
          <p className="text-sm text-danger">
            {errors.confirmPassword.message}
          </p>
        )}
      </div>
      <Button type="submit" size={"lg"}>
        Create account
      </Button>
    </form>
  );
}
